package paulo.baena.todo.persistence

import cats.effect.*
import com.zaxxer.hikari.HikariConfig
import doobie.*
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import paulo.baena.todo.config.DatabaseConfig

trait Database[F[_]] {
  def transactor(config: DatabaseConfig): Resource[F, HikariTransactor[F]]
  def startDatabaseAndMigrations(config: DatabaseConfig): F[Unit]
}

object Database {
  def apply[F[_]: Async]: Database[F] = new Database[F] {
    def transactor(config: DatabaseConfig): Resource[F, HikariTransactor[F]] =
      for {
        hikariConfig <- Resource.pure {
                          val buildConfig = new HikariConfig()
                          buildConfig.setDriverClassName(config.driver)
                          buildConfig.setJdbcUrl(config.url)
                          buildConfig.setUsername(config.user)
                          buildConfig.setPassword(config.password)
                          buildConfig
                        }
        xa           <- HikariTransactor.fromHikariConfig[F](hikariConfig)
      } yield xa

    def startDatabaseAndMigrations(config: DatabaseConfig): F[Unit] = Sync[F].delay {
      Flyway
        .configure()
        .dataSource(config.url, config.user, config.password)
        .load()
        .migrate()
    }
  }
}
