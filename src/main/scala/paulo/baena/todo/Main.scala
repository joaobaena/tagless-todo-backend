package paulo.baena.todo

import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.ember.server.*
//import org.http4s.server.middleware.{CORS, CORSConfig}
import paulo.baena.todo.api.Routes
import paulo.baena.todo.config.ApplicationConfig
import paulo.baena.todo.persistence.*

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val configLoader   = ApplicationConfig.configLoader[IO]
    val serverResource =
      for {
        databaseConfig   <- Resource.eval(configLoader.loadDatabaseConfig)
        httpServerConfig <- Resource.eval(configLoader.loadHttpServerConfig)
        database          = Database[IO](databaseConfig)
        _                <- Resource.eval(database.startDatabaseAndMigrations)
        transactor       <- database.transactor
        repository        = InMemoryTodoRepository[IO](transactor)
        httpApp           = Routes.live(repository).orNotFound
//        config            = CORSConfig.default
//                              .withAnyOrigin(false)
//                              .withAllowedOrigins(Set("http://www.todobackend.com"))
        server           <- EmberServerBuilder
                              .default[IO]
                              .withHost(Host.fromString(httpServerConfig.url).get)
                              .withPort(Port.fromInt(httpServerConfig.port).get)
                              .withHttpApp(httpApp)
//                              .withHttpApp(CORS(httpApp, config))
                              .build
      } yield server
    serverResource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
