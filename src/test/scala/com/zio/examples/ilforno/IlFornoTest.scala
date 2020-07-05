package com.zio.examples.ilforno

import com.zio.examples.ilforno.Ingredient.{Cheese, Tomato, Tuna}
import com.zio.examples.ilforno.fridge.Error.UnavailableIngredient
import com.zio.examples.ilforno.fridge._
import zio.{Promise, UIO, ZIO}
import zio.test.DefaultRunnableSpec
import zio.test._
import zio.test.environment.{TestClock, TestConsole, TestEnvironment}
import zio.test.Assertion._
import zio.duration._

object IlFornoTest extends DefaultRunnableSpec {
  def spec = suite("IlFornoTest")(
    testM("prepare pizza using available ingredients should succeed") {
      val request = Request("customer#1", PizzaType.Tonno)
      val availableIngredients = Map(Tuna -> 1, Tomato -> 3, Cheese -> 1)
      assertM(System.preparePizza(request, availableIngredients).run)(
        succeeds(equalTo(Map(Tuna -> 0, Tomato -> 0, Cheese -> 0)))
      )
    },
    testM("prepare pizza requires unavailable ingredients should fail") {
      val request = Request("customer#1", PizzaType.Tonno)
      val availableIngredients: Map[Ingredient, Int] = Map(Cheese -> 1)
      assertM(System.preparePizza(request, availableIngredients).run)(
        fails(isSubtype[UnavailableIngredient](anything))
      )
    },
    testM("start the system and handle a request should work correctly") {
      val request = Request("customer#1", PizzaType.QuatreFromage)
      val availableIngredients = Map(Tuna -> 10, Tomato -> 30, Cheese -> 10)
      val expectedIngredients = Map(Tuna -> 10, Tomato -> 30, Cheese -> 6)
      val initialState = FridgeWithState(availableIngredients, State.Closed)
      (for {
        system <- System.start
        _ <- system.sendRequest(request)
        p <- Promise.make[Nothing, String]
        _ <- system
          .handleRequests(name => p.succeed(name).unit, (_, _) => UIO.unit)
          .fork
        fridge <- ZIO.service[fridge.Service]
        customer <- p.await
        fridgeWithState <- fridge.fridgeState
        output <- TestConsole.output
      } yield
        assert(customer)(equalTo("customer#1")) && assert(
          fridgeWithState.state
        )(equalTo(State.Closed)) &&
          assert(fridgeWithState.ingredients)(equalTo(expectedIngredients)) &&
          assert(output)(contains("The fridge is closed.\n")))
        .provideSomeLayer[TestEnvironment](fridge.live(initialState))
    },
    testM("start the system and handle 2 requests should work correctly") {
      val request1 = Request("customer#1", PizzaType.QuatreFromage)
      val request2 = Request("customer#2", PizzaType.Margherita)
      val availableIngredients = Map(Tuna -> 10, Tomato -> 30, Cheese -> 10)
      val expectedIngredients1 = Map(Tuna -> 10, Tomato -> 30, Cheese -> 6)
      val expectedIngredients2 = Map(Tuna -> 10, Tomato -> 25, Cheese -> 5)

      val initialState = FridgeWithState(availableIngredients, State.Closed)
      (for {
        system <- System.start
        _ <- system.sendRequest(request1)
        _ <- system.sendRequest(request2)
        p1 <- Promise.make[Nothing, String]
        p2 <- Promise.make[Nothing, String]
        promises = Map(request1.name -> p1, request2.name -> p2)
        _ <- system
          .handleRequests(
            name => promises(name).succeed(name).unit,
            (_, _) => UIO.unit
          )
          .fork
        fridge <- ZIO.service[fridge.Service]
        customer1 <- p1.await
        fridgeWithState1 <- fridge.fridgeState
        _ <- TestClock.adjust(15.seconds)
        customer2 <- p2.await
        fridgeWithState2 <- fridge.fridgeState
        output <- TestConsole.output
      } yield
        assert(customer1)(equalTo("customer#1")) && assert(
          fridgeWithState1.state
        )(equalTo(State.Closed)) &&
          assert(fridgeWithState1.ingredients)(equalTo(expectedIngredients1)) &&
          assert(customer2)(equalTo("customer#2")) && assert(
          fridgeWithState2.state
        )(equalTo(State.Closed)) &&
          assert(fridgeWithState2.ingredients)(equalTo(expectedIngredients2)) &&
          assert(output.count(_.contains("The fridge is closed.\n")))(
            equalTo(2)
          ))
        .provideSomeLayer[TestEnvironment](fridge.live(initialState))
    },
    testM(
      "start the system and handle 2 requests in parallel should work correctly"
    ) {
      val request1 = Request("customer#1", PizzaType.QuatreFromage)
      val request2 = Request("customer#2", PizzaType.Margherita)
      val availableIngredients = Map(Tuna -> 10, Tomato -> 30, Cheese -> 10)
      val expectedIngredients = Map(Tuna -> 10, Tomato -> 25, Cheese -> 5)

      val initialState = FridgeWithState(availableIngredients, State.Closed)
      (for {
        system <- System.start
        _ <- system.sendRequest(request1).zipPar(system.sendRequest(request2))
        p1 <- Promise.make[Nothing, String]
        p2 <- Promise.make[Nothing, String]
        promises = Map(request1.name -> p1, request2.name -> p2)
        _ <- system
          .handleRequests(
            name => promises(name).succeed(name).unit,
            (_, _) => UIO.unit
          )
          .fork
        _ <- TestClock.adjust(15.seconds)
        fridge <- ZIO.service[fridge.Service]
        customer1 <- p1.await
        customer2 <- p2.await
        fridgeWithState <- fridge.fridgeState
        output <- TestConsole.output
      } yield
        assert(customer1)(equalTo("customer#1")) &&
          assert(customer2)(equalTo("customer#2")) && assert(
          fridgeWithState.state
        )(equalTo(State.Closed)) &&
          assert(fridgeWithState.ingredients)(equalTo(expectedIngredients)) &&
          assert(output.count(_.contains("The fridge is closed.\n")))(
            equalTo(2)
          ))
        .provideSomeLayer[TestEnvironment](fridge.live(initialState))
    },
    testM(
      "start the system and handle a request with a wrong fridge state should fail"
    ) {
      val request = Request("customer#1", PizzaType.QuatreFromage)
      val availableIngredients = Map(Tuna -> 10, Tomato -> 30, Cheese -> 10)
      val initialState = FridgeWithState(availableIngredients, State.Opened)
      (for {
        system <- System.start
        _ <- system.sendRequest(request)
        p <- Promise.make[Nothing, String]
        _ <- system
          .handleRequests(_ => UIO.unit, (_, name) => p.succeed(name).unit)
          .fork
        fridge <- ZIO.service[fridge.Service]
        customer <- p.await
        fridgeWithState <- fridge.fridgeState
        output <- TestConsole.output
      } yield
        assert(customer)(equalTo("customer#1")) && assert(
          fridgeWithState.state
        )(equalTo(State.Closed)) &&
          assert(fridgeWithState.ingredients)(equalTo(availableIngredients)) &&
          assert(output)(contains("The fridge is closed.\n")))
        .provideSomeLayer[TestEnvironment](fridge.live(initialState))
    },
  )
}
