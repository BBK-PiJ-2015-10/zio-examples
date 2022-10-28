ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val zioVersion = "2.0.2"

lazy val root = (project in file("."))
  .settings(
    name := "tech-challenge-server",
    libraryDependencies ++= Seq(

      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-test" % zioVersion % Test,

      "dev.zio" %% "zio-streams" % zioVersion,

      "dev.zio" %% "zio-json" % "0.3.0-RC8",

     // This is the one for the server
     "io.d11" %% "zhttp" % "2.0.0-RC11",


    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
