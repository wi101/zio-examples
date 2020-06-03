package com.zio.examples.ilforno

import com.zio.examples.ilforno.Ingredient.{Cheese, Tomato, Tuna}
import zio.test.Assertion._
import zio.test._

object IlFornoSpec extends DefaultRunnableSpec {

  def spec =
    suite("IlFornoSpec")(
      testM("preparePizza invalid") {
        val ingredients: Map[Ingredient, Int] = Map(Ingredient.Tomato -> 3)
        assertM(
          System.preparePizza(Request("wiem", PizzaType.Tonno), ingredients).run
        )(fails(isSubtype[UnavailableIngredient](anything)))
      },
      testM("preparePizza valid") {
        val ingredients: Map[Ingredient, Int] =
          Map(Tuna -> 1, Tomato -> 3, Cheese -> 1)

        val result: Map[Ingredient, Int] =
          Map(Tuna -> 0, Tomato -> 0, Cheese -> 0)
        assertM(
          System.preparePizza(Request("wiem", PizzaType.Tonno), ingredients)
        )(equalTo(result))
      }
    )
}
