package com.zio.examples.ilforno

import com.zio.examples.ilforno.Ingredient.{Cheese, Tomato, Tuna}
import com.zio.examples.ilforno.fridge.Error.WrongState
import com.zio.examples.ilforno.fridge.{FridgeWithState, State, _}
import zio.clock.Clock
import zio.console.Console
import zio.duration._
import zio.test.Assertion._
import zio.test._
import zio._

object IlFornoSpec extends DefaultRunnableSpec {

  def spec =
    suite("IlFornoSpec")(
      testM("preparePizza invalid") {
        val ingredients: Map[Ingredient, Int] = Map(Ingredient.Tomato -> 3)
        assertM(
          System.preparePizza(Request("wiem", PizzaType.Tonno), ingredients).run
        )(fails(isSubtype[Error.UnavailableIngredient](anything)))
      },
      testM("preparePizza valid") {
        val ingredients: Map[Ingredient, Int] =
          Map(Tuna -> 1, Tomato -> 3, Cheese -> 1)

        val result: Map[Ingredient, Int] =
          Map(Tuna -> 0, Tomato -> 0, Cheese -> 0)
        assertM(
          System.preparePizza(Request("wiem", PizzaType.Tonno), ingredients)
        )(equalTo(result))
      },
      testM("handleRequest") {
        val ingredients: Map[Ingredient, Int] =
          Map(Tuna -> 5, Tomato -> 3, Cheese -> 2)
        val expectedResult = Map(Tuna -> 4, Tomato -> 0, Cheese -> 1)
        val initialFridgeState =
          fridge.FridgeWithState(ingredients, State.Closed)
        (for {
          system <- System.start
          request = Request("customer#1", PizzaType.Tonno)
          _ <- system.sendRequest(request)
          fridge <- ZIO.service[fridge.Service]
          _ <- system.handleRequests((_, _) => UIO.unit).fork
          ingredients <- fridge.fridgeState
            .repeat(
              Schedule
                .doUntil[FridgeWithState](_.ingredients == expectedResult)
            )
            .map(_.ingredients)

        } yield assert(ingredients)(hasSameElements(expectedResult)))
          .provideSomeLayer[Console](
            Clock.live ++ fridge.live(initialFridgeState)
          )
      },
      testM("handle requests should fail with wrong state") {
        val ingredients: Map[Ingredient, Int] =
          Map(Tuna -> 5, Tomato -> 3, Cheese -> 2)
        val initialFridgeState =
          fridge.FridgeWithState(ingredients, State.Opened)

        (for {
          system <- System.start
          request = Request("customer#2", PizzaType.Tonno)
          _ <- system.sendRequest(request)
          fridge <- ZIO.service[fridge.Service]
          p <- Promise.make[Nothing, Error]
          _ <- system.handleRequests((e, _) => p.succeed(e).unit).fork
          fridgeState <- fridge.fridgeState.delay(300.millis)
          error <- p.await
        } yield
          assert(fridgeState.state)(equalTo(State.Closed)) && assert(
            fridgeState.ingredients
          )(equalTo(ingredients)) && assert(error)(
            equalTo(WrongState(State.Opened, State.Closed))
          ))
          .provideSomeLayer[Console](
            Clock.live ++ fridge.live(initialFridgeState)
          )
      }
    )
}
