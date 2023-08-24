package paulo.baena.todo.persistence

import cats.effect.*
import doobie.*
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import paulo.baena.todo.config.DatabaseConfig

object Database {
  def transactor[F[_]: Async](config: DatabaseConfig): Resource[F, HikariTransactor[F]] =
    for {
      // TODO: Check how to use CE3 executors
      ce <- ExecutionContexts.fixedThreadPool[F](4)
      xa <- HikariTransactor.newHikariTransactor[F](
              driverClassName = config.driver,
              url = config.url,
              user = config.user,
              pass = config.password,
              connectEC = ce
            )
    } yield xa
}
