package paulo.baena.todo

import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.ember.server.*
import org.http4s.server.middleware.CORS
import paulo.baena.todo.api.Routes
import paulo.baena.todo.config.ApplicationConfig
import paulo.baena.todo.persistence.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
    val configLoader                              = ApplicationConfig.configLoader[IO]
    val serverResource                            =
      for {
        databaseConfig   <- Resource.eval(configLoader.loadDatabaseConfig)
        httpServerConfig <- Resource.eval(configLoader.loadHttpServerConfig)
        database          = Database[IO](databaseConfig)
        _                <- Resource.eval(database.startDatabaseAndMigrations)
        transactor       <- database.transactor
        repository        = H2TodoRepository[IO](transactor)
        config           <- Resource.eval(
                              CORS.policy
                                .withAllowOriginAll(Routes.live(repository).orNotFound)
                            )
        server           <- EmberServerBuilder
                              .default[IO]
                              .withHost(Host.fromString(httpServerConfig.url).get)
                              .withPort(Port.fromInt(httpServerConfig.port).get)
                              .withHttpApp(config)
                              .build
      } yield server
    serverResource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
