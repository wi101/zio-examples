package com.zio.examples.http4s_doobie

import doobie.h2.H2Transactor
import org.flywaydb.core.Flyway
import scala.concurrent.ExecutionContext
import zio._
import cats.effect.Blocker

package object configuration {

  case class Config(api: ApiConfig, dbConfig: DbConfig)
  case class ApiConfig(endpoint: String, port: Int)
  case class DbConfig(
      url: String,
      user: String,
      password: String
  )

  def loadConfig: RIO[Configuration, Config] = RIO.accessM(_.config.load)

  def initDB(conf: DbConfig): Task[Unit] =
    Task.effect {
      Flyway
        .configure()
        .dataSource(conf.url, conf.user, conf.password)
        .load()
        .migrate()
    }.unit

  def mkTransactor(
      conf: DbConfig,
      connectEC: ExecutionContext,
      transactEC: ExecutionContext
  ): Managed[Throwable, H2Transactor[Task]] = {
    import zio.interop.catz._

    val xa = H2Transactor
      .newH2Transactor[Task](conf.url, conf.user, conf.password, connectEC, Blocker.liftExecutionContext(transactEC))

    val res = xa.allocated.map {
      case (transactor, cleanupM) =>
        Reservation(ZIO.succeed(transactor), _ => cleanupM.orDie)
    }.uninterruptible

    Managed(res)
  }

}
