package com.zio.examples.http4s_doobie.configuration
import zio.{RIO, Task, ZLayer}
import pureconfig.loadConfigOrThrow

final class PureConfig  extends Service {
  import pureconfig.generic.auto._
  val load: Task[Config] = Task.effect(loadConfigOrThrow[Config])
}

object PureConfig {
  val layer: ZLayer.NoDeps[Nothing, Configuration] = ZLayer.succeed(new PureConfig)
}

