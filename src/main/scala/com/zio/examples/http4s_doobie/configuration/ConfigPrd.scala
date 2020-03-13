package com.zio.examples.http4s_doobie.configuration
import pureconfig.loadConfigOrThrow
import zio.{Has, Layer, Task, ZLayer}
import pureconfig.generic.auto._

object ConfigPrd {
  val live: Layer[Throwable, Configuration] = ZLayer.fromEffectMany(
    Task
      .effect(loadConfigOrThrow[Config])
      .map(c => Has(c.api) ++ Has(c.dbConfig)))
}
