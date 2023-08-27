ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0"

val catsVersion        = "3.5.0"
val chimneyVersion     = "0.8.0-M1"
val circeVersion       = "0.14.5"
val circeConfigVersion = "0.10.0"
val doobieVersion      = "1.0.0-RC4"
val flywayVersion      = "9.19.1"
val http4sVersion      = "1.0.0-M40"
val h2Version          = "2.1.214"
val logCatsVersion     = "2.6.0"
val munitVersion       = "1.0.7"
val scalaCheckVersion  = "1.15.4"
val slfjVersion        = "1.7.32"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "tagless-todo-backend",
//    scalacOptions ++= Seq(
//      "-deprecation",
//      "-feature",
//      "-unchecked",
//      "-Wunused:imports" // Emit warning for unused imports.
//    ),
    libraryDependencies ++= Seq(
      "com.h2database"  % "h2"                  % h2Version,
      "org.typelevel"  %% "cats-effect"         % catsVersion,
      "org.typelevel"  %% "cats-effect-kernel"  % catsVersion,
      "org.typelevel"  %% "cats-effect-std"     % catsVersion,
      "org.typelevel"  %% "log4cats-slf4j"      % logCatsVersion,
      "org.flywaydb"    % "flyway-core"         % flywayVersion,
      "org.http4s"     %% "http4s-circe"        % http4sVersion,
      "org.http4s"     %% "http4s-core"         % http4sVersion,
      "org.http4s"     %% "http4s-dsl"          % http4sVersion,
      "org.http4s"     %% "http4s-ember-server" % http4sVersion,
      "io.circe"       %% "circe-config"        % circeConfigVersion,
      "io.circe"       %% "circe-core"          % circeVersion,
      "io.circe"       %% "circe-generic"       % circeVersion,
      "io.scalaland"   %% "chimney"             % chimneyVersion,
      "org.slf4j"       % "slf4j-log4j12"       % slfjVersion,
      "org.tpolecat"   %% "doobie-core"         % doobieVersion,
      "org.tpolecat"   %% "doobie-h2"           % doobieVersion,
      "org.tpolecat"   %% "doobie-hikari"       % doobieVersion,
      "org.tpolecat"   %% "doobie-scalatest"    % doobieVersion % Test,
      "org.typelevel"  %% "munit-cats-effect-3" % munitVersion  % Test,
      "org.scalacheck" %% "scalacheck"          % "1.15.4"      % Test
    )
  )

inThisBuild(
  List(
    scalaVersion      := "3.3.0",
    semanticdbEnabled := true
  )
)
