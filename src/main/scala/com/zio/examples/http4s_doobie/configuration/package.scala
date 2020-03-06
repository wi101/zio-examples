package com.zio.examples.http4s_doobie

import zio.{Has, RIO, Task}

package object configuration {

  type Configuration = Has[Service]

  case class Config(api: ApiConfig, dbConfig: DbConfig)
  case class ApiConfig(endpoint: String, port: Int)
  case class DbConfig(
      url: String,
      user: String,
      password: String
  )

  trait Service {
    val load: Task[Config]
  }

  def loadConfig: RIO[Configuration, Config] = RIO.accessM(_.get.load)
}
