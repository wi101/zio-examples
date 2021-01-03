package com.zio.examples.quickstart

import zio.{ Has, Ref, ZIO, ZLayer }

/**
  *
  *             |||||||     ||
  *                 |||     ||
  *                |||      ||        |||||     ||  |||    |||||      ||||
  *                |||      ||        |||||     ||  ||     |||||      ||||
  *               |||       ||           ||     ||| ||    ||||||      ||
  *              |||        ||        |||||      |||||    ||||||      ||
  *              |||        ||       ||| ||      ||||     |||         ||
  *             |||||||     ||       ||||||      ||||     |||| |      ||
  *             |||||||     ||||||   |||||||      ||       |||||      ||
  *                                               ||
  */
object ZIOLayers {

  // ZIO[-R, +E, +A]= Requires a dependency of type R, might fail with an error E,
  // or succeed with a value A

  // Service
  trait Show {
    def display(message: String): ZIO[Any, Nothing, Unit]
  }

  // Helper
  object Show {
    def display(message: String): ZIO[Has[Show], Nothing, Unit] =
      ZIO.accessM[Has[Show]](_.get.display(message))
  }
  // Implementation 1 / Constructor 1
  val layer1: ZLayer[Any, Nothing, Has[Show]] = ZLayer.succeed(new Show {
    override def display(message: String): ZIO[Any, Nothing, Unit] =
      ZIO.effectTotal(println(message))
  })

  // Implementation 2 / Constructor 2

  // Service: LinesPersistence
  type Lines = List[String]
  trait LinesPersistence {
    def update(toState: Lines => Lines): ZIO[Any, Nothing, Unit]
    def get: ZIO[Any, Nothing, Lines]
  }
  object LinesPersistence {
    case class LinesPersistenceUsingRef(lines: zio.Ref[List[String]]) extends LinesPersistence {
      override def update(toState: Lines => Lines): ZIO[Any, Nothing, Unit] =
        lines.update(toState)

      override def get: ZIO[Any, Nothing, Lines] =
        lines.get
    }
    val layer: ZLayer[Any, Nothing, Has[LinesPersistence]] =
      Ref.make(List.empty[String]).map(LinesPersistenceUsingRef).toLayer
  }
  case class ShowTest(lines: LinesPersistence) extends Show {
    override def display(message: String): ZIO[Any, Nothing, Unit] =
      lines.update(_ :+ message)
  }

  val layer2: ZLayer[Has[LinesPersistence], Nothing, Has[Show]] =
    ZLayer.fromService(ShowTest)

  // Implementation 3 / Constructor 3

  case class EmbroideryConfig(art: Char, spaces: Int)

  val layer3: ZLayer[Has[EmbroideryConfig], Nothing, Has[Show]] =
    ZLayer.fromService(config =>
      new Show {
        import embroidery._
        override def display(message: String): ZIO[Any, Nothing, Unit] =
          ZIO.effectTotal(
            println(message.toAsciiArt(art = config.art, spaces = config.spaces))
          )
      }
    )
  def show(message: String): ZIO[Has[Show], Nothing, Unit] =
    Show.display(message)

  val showUsingLayer1: ZIO[Any, Nothing, Unit] =
    show("Hello World").provideLayer(layer1)

  val showUsingLayer2: ZIO[Any, Nothing, Boolean] =
    (for {
      state <- ZIO.service[LinesPersistence]
      _     <- (show("Hi!") *> show("Bonjour!"))
      lines <- state.get
    } yield lines == List("Hi!", "Bonjour!"))
      .provideLayer(LinesPersistence.layer >+> layer2)

  val showUsingLayer3: ZIO[Any, Nothing, Unit] = {
    val configLayer = ZLayer.succeed(EmbroideryConfig(art = '*', spaces = 1))
//    show("ZLayer").provideLayer(configLayer >>> layer3)
    // OR
    show("ZLayer")
      .provideSomeLayer[Has[EmbroideryConfig]](layer3)
      .provideLayer(configLayer)
  }
}
