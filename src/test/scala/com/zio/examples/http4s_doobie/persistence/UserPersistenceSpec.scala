package com.zio.examples.http4s_doobie
package persistence
import com.zio.examples.http4s_doobie.configuration.{ Configuration, DbConfig }
import doobie.util.transactor.Strategy.before
import zio.blocking.Blocking
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment
import zio.{ Cause, ZLayer }

object UserPersistenceSpec extends DefaultRunnableSpec {

  def spec =
    suite("Persistence integration test")(testM("Persistense Live") {
      for {
        _        <- UserPersistenceService.createUserTable
        notFound <- getUser(100).either
        created  <- createUser(User(14, "usr")).either
        deleted  <- deleteUser(14).either
      } yield assert(notFound)(isLeft(anything)) &&
        assert(created)(isRight(equalTo(User(14, "usr")))) &&
        assert(deleted)(isRight(isTrue))
    }).provideSomeLayer[TestEnvironment](
      (Configuration.live >+> Blocking.live >+> UserPersistenceService.transactorLive >+> UserPersistenceService.live)
        .mapError(_ => TestFailure.Runtime(Cause.die(new Exception("die"))))
    )

}
