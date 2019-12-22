package com.zio.examples.http4s_doobie.configuration
import zio.{Task, RIO}
import pureconfig.loadConfigOrThrow

trait Configuration extends Serializable {
  val config: Configuration.Service[Any]
}
object Configuration {
  trait Service[R] {
    val load: RIO[R, Config]
  }

  trait Live extends Configuration {
    val config: Service[Any] = new Service[Any] {
      import pureconfig.generic.auto._

      val load: Task[Config] = Task.effect(loadConfigOrThrow[Config])
    }
  }

  object Live extends Live

  trait Test extends Configuration {
    val config: Service[Any] = new Service[Any] {
      val load: Task[Config] = Task.effectTotal(
        Config(ApiConfig("loacalhost", 8080), DbConfig("localhost", "", "")))
    }
  }

}
