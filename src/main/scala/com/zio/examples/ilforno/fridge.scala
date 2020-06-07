package com.zio.examples.ilforno

import com.zio.examples.ilforno.fridge.Error.WrongState
import zio._
import zio.console.Console

object fridge {
  type Fridge = Has[Service]

  sealed trait Error
  object Error {
    case class UnavailableIngredient(ingredient: Ingredient) extends Error
    case class WrongState(state: State, expected: State) extends Error
  }

  sealed trait State
  object State {
    case object Closed extends State
    case object Opened extends State
  }

  case class FridgeWithState(ingredients: Map[Ingredient, Int], state: State)
  trait Service {
    protected def currentState: Ref[FridgeWithState]
    def open: IO[WrongState, Map[Ingredient, Int]]
    def close: URIO[Console, Unit]
    def set(newIngredients: Map[Ingredient, Int]): IO[WrongState, Unit]
    def fridgeState: UIO[FridgeWithState]
  }

  def checkState(state: State, expected: State): IO[WrongState, State] =
    if (state == expected) IO.succeed(state)
    else IO.fail(WrongState(state, expected))

  def live(initialState: Ref[FridgeWithState]): Layer[Nothing, Fridge] =
    ZLayer.succeed(new Service {
      override def currentState: Ref[FridgeWithState] = initialState
      override def open: IO[WrongState, Map[Ingredient, Int]] =
        for {
          fridgeWithState <- currentState.get
          _ <- checkState(fridgeWithState.state, State.Closed)
          result <- currentState.updateAndGet(_.copy(state = State.Opened))
        } yield result.ingredients

      override def close: URIO[Console, Unit] =
        currentState
          .updateAndGet(_.copy(state = State.Closed))
          .unit *> console.putStrLn("The fridge is closed.")

      override def set(
        newIngredients: Map[Ingredient, Int]
      ): IO[WrongState, Unit] =
        for {
          fridgeWithState <- currentState.get
          _ <- checkState(fridgeWithState.state, State.Opened)
          _ <- currentState.updateAndGet(_.copy(ingredients = newIngredients))
        } yield ()

      override def fridgeState: UIO[FridgeWithState] =
        currentState.get
    })

}
