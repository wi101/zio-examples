package com.zio.examples.http4s_doobie

import cats.effect.ExitCode
import com.zio.examples.http4s_doobie.configuration.Configuration
import com.zio.examples.http4s_doobie.db.Persistence
import com.zio.examples.http4s_doobie.http.Api
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.putStrLn
import zio.interop.catz._
import zio._

object Main extends App {

  type AppEnvironment = Clock with Blocking with Persistence

  type AppTask[A] = RIO[AppEnvironment, A]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val program: ZIO[ZEnv, Throwable, Unit] = for {
      conf <- configuration.loadConfig.provide(Configuration.Live)

      blockingEnv <- ZIO.environment[Blocking]
      blockingEC <- blockingEnv.blocking.blockingExecutor.map(_.asEC)

      transactorR = Persistence.mkTransactor(
        conf.dbConfig,
        platform.executor.asEC,
        blockingEC
      )

      httpApp = Router[AppTask](
        "/users" -> Api(s"${conf.api.endpoint}/users").route
      ).orNotFound

      server = ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
        BlazeServerBuilder[AppTask]
          .bindHttp(conf.api.port, "0.0.0.0")
          .withHttpApp(CORS(httpApp))
          .serve
          .compile[AppTask, AppTask, ExitCode]
          .drain
      }
      program <- transactorR.use { transactor =>
        server.provideSome[ZEnv] { _ =>
          new Clock.Live with Blocking.Live
          with Persistence.Live {
            override protected def tnx: doobie.Transactor[Task] = transactor
          }
        }
      }
    } yield program

    program.foldM(
      err => putStrLn(s"Execution failed with: $err") *> IO.succeed(1),
      _ => IO.succeed(0)
    )
  }
}
