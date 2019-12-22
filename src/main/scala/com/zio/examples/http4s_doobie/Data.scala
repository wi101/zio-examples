package com.zio.examples.http4s_doobie

final case class User(id: Long, name: String)

final case class UserNotFound(id: Int) extends Exception
