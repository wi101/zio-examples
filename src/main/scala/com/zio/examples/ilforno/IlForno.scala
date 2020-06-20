package com.zio.examples.ilforno

import com.zio.examples.ilforno.fridge._
import zio.clock.Clock
import zio.duration._
import zio._

sealed trait Ingredient extends Serializable with Product
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

case class Request(name: String, pizza: PizzaType)

class System(requests: Queue[Request]) {

  def sendRequest(request: Request): UIO[Unit] = requests.offer(request).unit

  def handleRequests[R](
    fallbackAction: (Error, String) => URIO[R, Unit]
  ): URIO[R with Clock with Fridge, Unit] =
    (for {
      request <- requests.take
      fridge <- ZIO.service[fridge.Service]
      resource = ZManaged.make(fridge.open)(_ => fridge.close)
      _ <- resource
        .use { oldState =>
          System
            .preparePizza(request, oldState)
            .flatMap(newState => fridge.set(newState))
        }
        .catchAll(e => fallbackAction(e, request.name))
        .ensuring(fridge.close)
    } yield ())
      .repeat(Schedule.spaced(15.seconds) && Schedule.duration(8.hours))
      .unit

}

object System {

  def start =
    Queue.bounded[Request](12).map(new System(_))

  def preparePizza(
    request: Request,
    ingredients: Map[Ingredient, Int]
  ): IO[Error, Map[Ingredient, Int]] =
    request.pizza.ingredients
      .foldLeft[IO[Error, Map[Ingredient, Int]]](UIO(ingredients)) {
        case (res, (ingr, count)) =>
          val availableIngr = ingredients.get(ingr).flatMap { c =>
            if ((c - count) >= 0) Some(c - count)
            else None
          }
          availableIngr.fold[IO[Error, Map[Ingredient, Int]]](
            IO.fail(Error.UnavailableIngredient(ingr))
          )(availableCount => res.map(_.updated(ingr, availableCount)))
      }

}
