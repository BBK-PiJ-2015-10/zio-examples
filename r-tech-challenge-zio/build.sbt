ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"


lazy val zioVersion = "2.0.0"

lazy val root = (project in file("."))
  .settings(
    name := "r-tech-challenge-zio",
    libraryDependencies ++= Seq(

      "dev.zio" %% "zio" % "2.0.2",
      "dev.zio" %% "zio-test" % "2.0.2" % Test,

      "dev.zio" %% "zio-streams" % zioVersion,

      "dev.zio" %% "zio-json" % "0.3.0-RC8",

      "io.d11" %% "zhttp" % "2.0.0-RC11",

      "org.scala-lang.modules" %% "scala-xml" % "2.1.0"

    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
