package com.zio.examples.http4s_doobie.configuration
import zio.{Task, RIO}
import pureconfig.loadConfigOrThrow

trait Configuration extends Serializable {
  val config: Configuration.Service
}
object Configuration {
  trait Service {
    val load: Task[Config]
  }

  trait Live extends Configuration {
    val config: Service = new Service {
      import pureconfig.generic.auto._

      val load: Task[Config] = Task.effect(loadConfigOrThrow[Config])
    }
  }

  object Live extends Live

  trait Test extends Configuration {
    val config: Service = new Service {
      val load: Task[Config] = Task.effectTotal(
        Config(ApiConfig("loacalhost", 8080), DbConfig("localhost", "", "")))
    }
  }

}
