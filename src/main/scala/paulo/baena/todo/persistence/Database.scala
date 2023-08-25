package paulo.baena.todo.persistence

import cats.effect.*
import cats.implicits.*
import com.zaxxer.hikari.HikariConfig
import doobie.*
import doobie.hikari.HikariTransactor
import doobie.implicits.toSqlInterpolator
import org.flywaydb.core.Flyway
import paulo.baena.todo.config.DatabaseConfig

trait Database[F[_]] {
  def transactor: Resource[F, HikariTransactor[F]]

  def startDatabaseAndMigrations: F[Unit]
}

object Database {
  def apply[F[_]: Async](databaseConfig: DatabaseConfig): Database[F] = new Database[F] {
    def transactor: Resource[F, HikariTransactor[F]] =
      for {
        hikariConfig <- Resource.pure {
                          val buildConfig = new HikariConfig()
                          buildConfig.setDriverClassName(databaseConfig.driver)
                          buildConfig.setJdbcUrl(databaseConfig.url)
                          buildConfig.setUsername(databaseConfig.user)
                          buildConfig.setPassword(databaseConfig.password)
                          buildConfig.setLeakDetectionThreshold(10000)
                          buildConfig
                        }
        xa           <- HikariTransactor.fromHikariConfig[F](hikariConfig)
      } yield xa

    def startDatabaseAndMigrations: F[Unit] = Sync[F].delay {
      Flyway
        .configure()
        .dataSource(databaseConfig.url, databaseConfig.user, databaseConfig.password)
        .load()
        .migrate()
    }
  }

}

object DatabaseHealthCheck {
  def healthCheck[F[_]: Sync](transactor: Transactor[F]): F[Boolean] = {
    val query = sql"SELECT 1".query[Int].unique
    transactor.trans.apply(query).attempt.map {
      case Right(_) => true
      case Left(_)  => false
    }
  }
}
