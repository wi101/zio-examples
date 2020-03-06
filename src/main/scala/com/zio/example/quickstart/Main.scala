package com.zio.example.quickstart
import zio._
import zio.duration._

object Main extends zio.App {

  def parseInt(s: String): ZIO[Any, String, Int] =
    ZIO(s.toInt).mapError(e => "oh no!")
  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {

    val io = for {
      _ <- console.putStrLn("Hello, give me a number")
      numberStr <- console.getStrLn
      _ <- console.putStrLn(s"Evaluating the number: $numberStr")
      number <- parseInt(numberStr)
      chocolate <- Chocolate(number)
      _ <- chocolate.eat
        .repeat(Schedule.spaced(1.second) && Schedule.recurs(9))
        .option
    } yield number

    io.retry(Schedule.recurs(2)).fold(_ => 1, _ => 0)

  }
}

case class Chocolate private (max: Int, state: Ref[Int]) {

  val eat = state.modify { oldState =>
    val (newState, action) =
      if (oldState < max)
        (oldState + 1, console.putStrLn("Eating " + (oldState + 1)))
      else
        (oldState,
         console.putStrLn("oh you ate all the chocolate!") *> ZIO.fail(
           "oh you ate all the chocolate!"))

    (action, newState)
  }.flatten
}

object Chocolate {
  def apply(max: Int) = Ref.make(0).map(state => new Chocolate(max, state))
}
