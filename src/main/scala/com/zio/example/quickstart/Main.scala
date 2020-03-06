package com.zio.example.quickstart
import zio._
import zio.duration._

object Main extends zio.App {

  def parseInt(s: String): Task[Int] = Task(s.toInt)
  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {

    val io = for {
      _ <- console.putStrLn("Hello, how many chocolate would you like to eat?!")
      numberStr <- console.getStrLn
      _ <- console.putStrLn(s"Evaluating the number >>> $numberStr")
      number <- parseInt(numberStr)
      chocolate <- Chocolate(number)
      _ <- chocolate.eat
        .repeat(Schedule.spaced(1.second) && Schedule.recurs(9))
        .option
    } yield number

    io.retry(Schedule.recurs(2))
      .foldM(
        _ =>
          console.putStrLn("you don't like chocolate? ok.... BYE! ") *> UIO(1),
        _ => console.putStrLn("I hope you enjoyed :-) ZIO!") *> UIO(0))

  }
}

case class Chocolate private (max: Int, state: Ref[Int]) {

  val eat = state.modify { oldState =>
    val (newState, action) =
      if (max <= 0)
        (oldState,
         console.putStrLn("No chocolate for you :( it was your choice.. well good decision :-D") *>
           ZIO.fail("no chocolate!"))
      else if (oldState < max)
        (oldState + 1, console.putStrLn("Eating.... " + (oldState + 1)))
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
