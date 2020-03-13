package com.zio.examples.http4s_doobie

final case class User(id: Int, name: String)

final case class UserNotFound(id: Int) extends Exception
