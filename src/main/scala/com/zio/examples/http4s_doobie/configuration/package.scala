package com.zio.examples.http4s_doobie

import zio.RIO

package object configuration {

  case class Config(api: ApiConfig, dbConfig: DbConfig)
  case class ApiConfig(endpoint: String, port: Int)
  case class DbConfig(
      url: String,
      user: String,
      password: String
  )

  def loadConfig: RIO[Configuration, Config] = RIO.accessM(_.config.load)
}
