ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0"

val catsVersion = "3.5.0"
val http4sVersion = "0.23.22"

lazy val root = (project in file("."))
  .settings(
    name := "tagless-todo-backend",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsVersion,
      "org.typelevel" %% "cats-effect-kernel" % catsVersion,
      "org.typelevel" %% "cats-effect-std" % catsVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion
    )
  )
