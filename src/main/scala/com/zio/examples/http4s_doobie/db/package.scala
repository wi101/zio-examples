package com.zio.examples.http4s_doobie

import zio.RIO

/**
  * Helper that will access to the Persistence Service
  */
package object db extends Persistence.Service[Persistence] {

  def get(id: Int): RIO[Persistence, User] = RIO.accessM(_.userPersistence.get(id))
  def create(user: User): RIO[Persistence, User] = RIO.accessM(_.userPersistence.create(user))
  def delete(id: Int): RIO[Persistence, Boolean] = RIO.accessM(_.userPersistence.delete(id))
}
