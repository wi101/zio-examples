package com.zio.examples.http4s_doobie
package db
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

object UserPersistenceSpec extends DefaultRunnableSpec {
  import UserPersistenceService._
  val ec = concurrent.ExecutionContext.global
  def spec =
    suite("Persistence integration test")(testM("Persistense Live") {
      for {
        notFound <- get(100).either
        created <- create(User(14, "usr")).either
        deleted <- delete(14).either
      } yield
        assert(notFound)(isLeft(anything)) &&
          assert(created)(isRight(equalTo(User(14, "usr")))) &&
          assert(deleted)(isRight(isTrue))
    }).provideSomeLayer[TestEnvironment](UserPersistenceService
      .layer(configuration.DbConfig("jdbc:h2:~/test", "", ""), ec, ec))
}
