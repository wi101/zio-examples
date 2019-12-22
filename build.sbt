name := "zio-examples"
version := "0.1"
scalaVersion := "2.12.6"

val ScalaZVersion = "7.2.26"
val Http4sVersion = "0.21.0-M6"
val CirceVersion = "0.12.0-M1"
val DoobieVersion = "0.8.8"
val ZIOVersion = "1.0.0-RC17"
val PureConfigVersion = "0.11.0"
val H2Version = "1.4.199"
val FlywayVersion = "6.0.0-beta2"

libraryDependencies ++= Seq(
  // ZIO
  "dev.zio" %% "zio" % ZIOVersion,
  "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC10",
  "dev.zio" %% "zio-test" % ZIOVersion % "test",
  "dev.zio" %% "zio-test-sbt" % ZIOVersion % "test",
  // Http4s
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s" %% "http4s-circe" % Http4sVersion,
  "org.http4s" %% "http4s-dsl" % Http4sVersion,
  // Circe
  "io.circe" %% "circe-generic" % CirceVersion,
  "io.circe" %% "circe-generic-extras" % CirceVersion,
  // Doobie
  "org.tpolecat" %% "doobie-core" % DoobieVersion,
  "org.tpolecat" %% "doobie-h2" % DoobieVersion,
  //pure config
  "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
  //h2
  "com.h2database" % "h2" % H2Version,
  //flyway
  "org.flywaydb" %  "flyway-core" % FlywayVersion,
  // log4j
  "org.slf4j" % "slf4j-log4j12" % "1.7.26"
)

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
