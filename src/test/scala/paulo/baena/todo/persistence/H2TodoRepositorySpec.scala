package paulo.baena.todo.persistence

import cats.effect.IO
import cats.implicits.*
import doobie.Transactor
import doobie.implicits.toSqlInterpolator
import doobie.scalatest.*
import org.flywaydb.core.Flyway
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers
import paulo.baena.todo.config.DatabaseConfig
import paulo.baena.todo.persistence.Representations.{CreateTodoCommand, UpdateTodoCommand}

class H2TodoRepositorySpec extends AnyFunSuite with Matchers with IOChecker with BeforeAndAfterAll {
  private val databaseConfig = DatabaseConfig(
    url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    user = "user",
    password = "password",
    driver = "org.h2.Driver"
  )

  override def transactor: Transactor[IO] = Transactor.fromDriverManager[IO](
    driver = databaseConfig.driver,
    url = databaseConfig.url,
    user = databaseConfig.user,
    password = databaseConfig.password,
    logHandler = None
  )

  override def beforeAll(): Unit =
    Flyway
      .configure()
      .dataSource(databaseConfig.url, databaseConfig.user, databaseConfig.password)
      .load()
      .migrate()

  test("CreateTodo")(check(H2TodoRepository.Queries.createTodo(CreateTodoCommand("test", 1.some))))

  test("GetTodoById")(check(H2TodoRepository.Queries.getTodoById(1)))

  test("GetAll")(check(H2TodoRepository.Queries.getAll))

  test("DeleteTodoById")(check(H2TodoRepository.Queries.deleteTodoById(1)))

  test("DeleteAll")(check(H2TodoRepository.Queries.deleteAll))

  test("UpdateTodo")(check(H2TodoRepository.Queries.updateTodo(1, UpdateTodoCommand("test1".some, None, None))))

}
