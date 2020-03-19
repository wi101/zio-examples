package com.zio.examples.http4s_doobie

import pureconfig.loadConfigOrThrow
import zio._

package object configuration {

  type Configuration = Has[ApiConfig] with Has[DbConfig]

  case class AppConfig(api: ApiConfig, dbConfig: DbConfig)
  case class ApiConfig(endpoint: String, port: Int)
  case class DbConfig(
      url: String,
      user: String,
      password: String
  )

  val apiConfig: ZIO[Has[ApiConfig], Throwable, ApiConfig] = ZIO.access(_.get)
  val dbConfig: ZIO[Has[DbConfig], Throwable, DbConfig] = ZIO.access(_.get)

  object Configuration {
    import pureconfig.generic.auto._
    val live: Layer[Throwable, Configuration] = ZLayer.fromEffectMany(
      Task
        .effect(loadConfigOrThrow[AppConfig])
        .map(c => Has(c.api) ++ Has(c.dbConfig)))
  }
}
