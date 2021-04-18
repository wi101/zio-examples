name := "zio-examples"
version := "0.1"
scalaVersion := "2.13.3"

val Http4sVersion     = "0.21.22"
val CirceVersion      = "0.13.0"
val DoobieVersion     = "0.12.1"
val ZIOVersion        = "1.0.6"
val PureConfigVersion = "0.15.0"
val ZIOInterop        = "2.4.0.0"

libraryDependencies ++= Seq(
  // ZIO
  "dev.zio"          %% "zio"              % ZIOVersion,
  "com.github.wi101" %% "embroidery"       % "0.1.1",
  "dev.zio"          %% "zio-interop-cats" % ZIOInterop,
  "dev.zio"          %% "zio-test"         % ZIOVersion % "test",
  "dev.zio"          %% "zio-test-sbt"     % ZIOVersion % "test",
  // Http4s
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s" %% "http4s-circe"        % Http4sVersion,
  "org.http4s" %% "http4s-dsl"          % Http4sVersion,
  // Circe
  "io.circe" %% "circe-generic"        % CirceVersion,
  "io.circe" %% "circe-generic-extras" % CirceVersion,
  // Doobie
  "org.tpolecat" %% "doobie-core" % DoobieVersion,
  "org.tpolecat" %% "doobie-h2"   % DoobieVersion,
  //pure config
  "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
  // log4j
  "org.slf4j" % "slf4j-log4j12" % "1.7.30"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint"
)
scalafmtOnCompile := true

// scalafix; run with `scalafixEnable` followed by `scalafixAll`
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.4.3"

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
