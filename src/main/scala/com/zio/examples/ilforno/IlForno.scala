package com.zio.examples.ilforno

import zio.clock.Clock
import zio.duration._

object ilforno {

  sealed trait Ingredient
  object Ingredient {
    final case object Tuna extends Ingredient
    final case object Tomato extends Ingredient
    final case object Cheese extends Ingredient
    final case object Onion extends Ingredient
    final case object Chili extends Ingredient
    final case object Mushroom extends Ingredient
  }

  sealed trait PizzaType {
    def ingredients: Map[Ingredient, Int]
  }

  object PizzaType {
    import Ingredient._

    case object Tonno extends PizzaType {
      val ingredients = Map(Tuna -> 1, Tomato -> 3, Cheese -> 1)
    }

    case object Margherita extends PizzaType {
      val ingredients = Map(Tomato -> 5, Cheese -> 1)
    }

    case object QuatreFromage extends PizzaType {
      val ingredients = Map(Cheese -> 4)
    }

    case object Vegetarian extends PizzaType {
      val ingredients = Map(Tomato -> 4, Mushroom -> 8, Chili -> 4, Cheese -> 1)
    }
  }

  final case class UnavailableIngredient(ingredient: Ingredient) {
    override def toString: String = s"Not enough: $ingredient"
  }

  case class Request(name: String, pizza: PizzaType)

  import zio._

  final class System(requests: Queue[Request],
                     currentIngredients: Ref[Map[Ingredient, Int]]) {

    def sendRequest(request: Request): UIO[Unit] = requests.offer(request).unit

    def handleRequests[R](
      fallbackAction: (UnavailableIngredient, String) => URIO[R, Unit]
    ): URIO[R with Clock, Unit] =
      (for {
        request <- requests.take
        oldState <- currentIngredients.get
        newState <- System
          .preparePizza(request, oldState)
          .catchAll(e => fallbackAction(e, request.name).as(oldState))
        _ <- currentIngredients.set(newState)
      } yield ())
        .repeat(Schedule.spaced(15.seconds) && Schedule.duration(8.hours))
        .unit

  }

  object System {

    def start(initialIngredients: Map[Ingredient, Int]) =
      for {
        requests <- Queue.bounded[Request](12)
        ingredients <- Ref.make(initialIngredients)
      } yield new System(requests, ingredients)

    def preparePizza(
      request: Request,
      ingredients: Map[Ingredient, Int]
    ): IO[UnavailableIngredient, Map[Ingredient, Int]] =
      request.pizza.ingredients
        .foldLeft[IO[UnavailableIngredient, Map[Ingredient, Int]]](
          UIO(ingredients)
        ) {
          case (res, (ingr, count)) =>
            val availableIngr = ingredients.get(ingr).flatMap { c =>
              if ((c - count) >= 0) Some(c - count)
              else None
            }
            availableIngr.fold[IO[UnavailableIngredient, Map[Ingredient, Int]]](
              IO.fail(UnavailableIngredient(ingr))
            )(availableCount => res.map(_.updated(ingr, availableCount)))

        }

  }

}
