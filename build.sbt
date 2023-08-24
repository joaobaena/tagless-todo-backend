ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0"

val catsVersion        = "3.5.0"
val chimneyVersion     = "0.8.0-M1"
val circeVersion       = "0.14.5"
val circeConfigVersion = "0.10.0"
val doobieVersion      = "1.0.0-RC4"
val flywayVersion      = "9.19.1"
val http4sVersion      = "0.23.22"
val h2Version          = "2.1.214"
val munitVersion       = "1.0.7"

lazy val root = (project in file("."))
  .settings(
    name := "tagless-todo-backend",
    libraryDependencies ++= Seq(
      "com.h2database" % "h2"                  % h2Version,
      "org.typelevel" %% "cats-effect"         % catsVersion,
      "org.typelevel" %% "cats-effect-kernel"  % catsVersion,
      "org.typelevel" %% "cats-effect-std"     % catsVersion,
      "org.flywaydb"   % "flyway-core"         % flywayVersion,
      "org.http4s"    %% "http4s-ember-server" % http4sVersion,
      "org.http4s"    %% "http4s-dsl"          % http4sVersion,
      "org.http4s"    %% "http4s-circe"        % http4sVersion,
      "io.circe"      %% "circe-config"        % circeConfigVersion,
      "io.circe"      %% "circe-core"          % circeVersion,
      "io.circe"      %% "circe-generic"       % circeVersion,
      "io.scalaland"  %% "chimney"             % chimneyVersion,
      "org.tpolecat"  %% "doobie-core"         % doobieVersion,
      "org.tpolecat"  %% "doobie-h2"           % doobieVersion,
      "org.tpolecat"  %% "doobie-hikari"       % doobieVersion,
      "org.typelevel" %% "munit-cats-effect-3" % munitVersion % Test
    )
  )
