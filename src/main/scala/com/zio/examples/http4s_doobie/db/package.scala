package com.zio.examples.http4s_doobie

import zio.{Has, RIO, Task}

/**
  * Helper that will access to the Persistence Service
  */
package object db {

  trait Service[A] {
    def get(id: Int): Task[A]
    def create(a: A): Task[A]
    def delete(id: Int): Task[Boolean]
  }


  type UserPersistence = Has[Service[User]]
  type ProductPersistence = Has[Service[Product]]
}
