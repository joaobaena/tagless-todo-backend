package paulo.baena.todo

import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.ember.server.*
import paulo.baena.todo.api.Routes
import paulo.baena.todo.config.ApplicationConfig
import paulo.baena.todo.persistence.*

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val configLoader = ApplicationConfig.configLoader[IO]

    for {
      databaseConfig   <- configLoader.loadDatabaseConfig
      httpServerConfig <- configLoader.loadHttpServerConfig
      _                <- Database[IO].startDatabaseAndMigrations(databaseConfig)
      repository       <- InMemoryTodoRepository.live[IO](Database[IO].transactor(databaseConfig)).use(IO.pure)
      httpApp           = Routes.live(repository).orNotFound
      res              <- EmberServerBuilder
                            .default[IO]
                            .withHost(Host.fromString(httpServerConfig.url).get)
                            .withPort(Port.fromInt(httpServerConfig.port).get)
                            .withHttpApp(httpApp)
                            .build
                            .use(_ => IO.never)
                            .as(ExitCode.Success)
    } yield res
  }
}
