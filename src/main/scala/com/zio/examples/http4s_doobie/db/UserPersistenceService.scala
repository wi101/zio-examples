package com.zio.examples.http4s_doobie
package db

import cats.effect.Blocker
import com.zio.examples.http4s_doobie.configuration.DbConfig
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.{Query0, Transactor, Update0}
import scala.concurrent.ExecutionContext
import zio._
import zio.interop.catz._

/**
  * Persistence Service
  */
final class UserPersistenceService(tnx: Transactor[Task])
    extends Service[User] {
  import UserPersistenceService._

  def get(id: Int): Task[User] =
    SQL
      .get(id)
      .option
      .transact(tnx)
      .foldM(
        err => Task.fail(err),
        maybeUser => Task.require(UserNotFound(id))(Task.succeed(maybeUser))
      )

  def create(user: User): Task[User] =
    SQL
      .create(user)
      .run
      .transact(tnx)
      .foldM(err => Task.fail(err), _ => Task.succeed(user))

  def delete(id: Int): Task[Boolean] =
    SQL
      .delete(id)
      .run
      .transact(tnx)
      .fold(_ => false, _ => true)
}

object UserPersistenceService {

  /**
    * Persistence Module for production using Doobie
    */
  object SQL {

    def get(id: Int): Query0[User] =
      sql"""SELECT * FROM USERS WHERE ID = $id """.query[User]

    def create(user: User): Update0 =
      sql"""INSERT INTO USERS (id, name) VALUES (${user.id}, ${user.name})""".update

    def delete(id: Int): Update0 =
      sql"""DELETE FROM USERS WHERE id = $id""".update
  }

  def mkTransactor(
      conf: DbConfig,
      connectEC: ExecutionContext,
      transactEC: ExecutionContext
  ) = {
    import zio.interop.catz._

    val xa = H2Transactor
      .newH2Transactor[Task](conf.url,
                             conf.user,
                             conf.password,
                             connectEC,
                             Blocker.liftExecutionContext(transactEC))

    val res = xa.allocated.map {
      case (transactor, cleanupM) =>
        Reservation(ZIO.succeed(transactor), _ => cleanupM.orDie)
    }.uninterruptible

    Managed(res).map(new UserPersistenceService(_)).orDie
  }

  def layer(conf: DbConfig,
            connectEC: ExecutionContext,
            transactEC: ExecutionContext
  ): ZLayer[Any, Nothing, UserPersistence] =
    ZLayer.fromManaged(mkTransactor(conf, connectEC, transactEC))

  def get(id: Int): RIO[UserPersistence, User] = RIO.accessM(_.get.get(id))
  def create(user: User): RIO[UserPersistence, User] = RIO.accessM(_.get.create(user))
  def delete(id: Int): RIO[UserPersistence, Boolean] = RIO.accessM(_.get.delete(id))

}
