package com.zio.examples.http4s_doobie

import zio.{Has, RIO}

/**
  * Helper that will access to the Persistence Service
  */
//to make it nicer you can specify the type Persistence in the toplevel package and use it here
package object db extends Persistence.Service[Has[Persistence.Service[Any]]] {

  type Persistence = Has[Persistence.Service[Any]]

  def get(id: Int): RIO[Persistence, User] = RIO.accessM(_.get.get(id))
  def create(user: User): RIO[Persistence, User] = RIO.accessM(_.get.create(user))
  def delete(id: Int): RIO[Persistence, Boolean] = RIO.accessM(_.get.delete(id))
}
