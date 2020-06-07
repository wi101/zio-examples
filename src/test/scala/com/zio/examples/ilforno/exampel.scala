package com.zio.examples.ilforno
import zio._

object example extends zio.App {
  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    (for {

      r <- Ref.make(1)
      _ <- r.get.bracket(s => UIO(println("the int is : " + s)))(_ => r.set(10))
    } yield ()).exitCode
}
