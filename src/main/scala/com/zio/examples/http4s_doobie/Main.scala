package com.zio.examples.http4s_doobie

import cats.effect.ExitCode
import com.zio.examples.http4s_doobie.configuration.{
  ApiConfig,
  Configuration
}
import com.zio.examples.http4s_doobie.persistence.{
  UserPersistence,
  UserPersistenceService
}
import com.zio.examples.http4s_doobie.http.Api
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.putStrLn
import zio.interop.catz._

object Main extends App {

  type AppEnvironment = Configuration with Clock with UserPersistence

  type AppTask[A] = RIO[AppEnvironment, A]

  val userPersistence = (Configuration.live ++ Blocking.live) >>> UserPersistenceService
    .live(platform.executor.asEC)

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val program: ZIO[AppEnvironment, Throwable, Unit] =
     for {
        api <- configuration.apiConfig
        httpApp = Router[AppTask](
          "/users" -> Api(s"${api.endpoint}/users").route
        ).orNotFound

        server <- ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
          BlazeServerBuilder[AppTask]
            .bindHttp(api.port, api.endpoint)
            .withHttpApp(CORS(httpApp))
            .serve
            .compile[AppTask, AppTask, ExitCode]
            .drain
        }
      } yield server

    program.provideSomeLayer[ZEnv](Configuration.live ++ userPersistence)foldM(
      err => putStrLn(s"Execution failed with: $err") *> IO.succeed(1),
      _ => IO.succeed(0)
    )
  }
}
