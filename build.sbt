import Dependencies._

ThisBuild / scalaVersion := "3.3.3"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "app",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.11",
      "org.typelevel" %% "cats-core" % "2.6.1",
      "org.typelevel" %% "cats-effect" % "3.3.14",
      "org.http4s" %% "http4s-blaze-server" % "0.23.12",
      "org.http4s" %% "http4s-blaze-client" % "0.23.0",
      "org.http4s" %% "http4s-circe" % "0.23.12",
      "org.http4s" %% "http4s-core" % "0.23.0",
      "org.http4s" %% "http4s-dsl" % "0.23.12",
      "io.circe" %% "circe-generic" % "0.14.1",
      "io.circe" %% "circe-parser" % "0.14.1",
      "com.lihaoyi" %% "scalatags" % "0.11.0"
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
