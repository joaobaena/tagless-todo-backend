ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "tagless-todo-backend",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.5.0",
      "org.typelevel" %% "cats-effect-kernel" % "3.5.0",
      "org.typelevel" %% "cats-effect-std" % "3.5.0"
    )
  )
