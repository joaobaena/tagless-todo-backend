package paulo.baena.todo.config

import cats.effect._
import io.circe.config.parser
import io.circe.generic.auto._

trait ApplicationConfig[F[_]] {
  def loadHttpServerConfig: F[HttpServerConfig]
  def loadDatabaseConfig: F[DatabaseConfig]
}

object ApplicationConfig {
  class ApplicationConfigLoader[F[_]: Sync] extends ApplicationConfig[F] {
    def loadHttpServerConfig: F[HttpServerConfig] = parser.decodePathF[F, HttpServerConfig]("http.server")

    def loadDatabaseConfig: F[DatabaseConfig] = parser.decodePathF[F, DatabaseConfig]("database")
  }

  def configLoader[F[_]: Sync] = new ApplicationConfigLoader[F]
}
