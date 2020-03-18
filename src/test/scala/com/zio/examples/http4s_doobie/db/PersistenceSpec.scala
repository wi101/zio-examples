package com.zio.examples.http4s_doobie
package db
import zio.test.Assertion._
import zio.test._

object PersistenceSpec extends DefaultRunnableSpec(
      suite("Persistence integration test")(testM("Persistense Live") {
        val ec = concurrent.ExecutionContext.global
        Persistence
          .mkTransactor(configuration.DbConfig("jdbc:h2:~/test", "", ""),
                        ec,
                        ec)
          .use {
            transaction =>
              (for {
                notFound <- db.get(100).either
                created <- db.create(User(13, "usr")).either
                deleted <- db.delete(13).either
              } yield
                assert(notFound, isLeft(anything)) && assert(
                  created,
                  isRight(equalTo(User(13, "usr")))) &&
                  assert(deleted, isRight(isTrue)))
                .provide(new Persistence.Live {
                  def tnx = transaction
                })
          }
      })
    )
