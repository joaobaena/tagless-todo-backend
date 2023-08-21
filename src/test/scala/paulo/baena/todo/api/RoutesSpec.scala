package paulo.baena.todo.api


import cats.Applicative
import cats.effect.*
import cats.effect.implicits.*
import cats.syntax.*
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.dsl.io.*
import org.http4s.circe.*
import io.circe.generic.auto.*
import io.circe.{Decoder, Encoder}
import paulo.baena.todo.api.Routes
import paulo.baena.todo.api.Messages.*
import paulo.baena.todo.persistence.Representations.{CreateTodoCommand, TodoItem, UpdateTodoCommand}
import paulo.baena.todo.persistence.TestTodoRepository

import java.time.{Instant, OffsetDateTime, ZoneOffset}
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class RoutesSpec[F[_]: Async] extends munit.FunSuite {
  // Define the dependencies
  val testRef: Ref[F, Map[Long, TodoItem]] = Ref.unsafe[F, Map[Long, TodoItem]](Map.empty)
  val testIdCounter: Ref[F, Long] = Ref.unsafe[F, Long](1L)

  val testClock: Clock[F] = new Clock[F] {
    override def realTime(unit: TimeUnit): F[Long] =
      Applicative[F].pure(System.nanoTime())
    override def monotonic(unit: TimeUnit): F[Long] =
      Applicative[F].pure(0L)
  }

  val testRepo: TestTodoRepository[F] = TestTodoRepository(testRef, testIdCounter, testClock)

  val routes: HttpApp[F] = new Routes[F] {}.httpRoutes(testRepo).orNotFound

  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]

  implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  def getRequest(uri: String): F[Response[F]] =
    routes.run(Request[F](Method.GET, Uri.unsafeFromString(uri)))

  def deleteRequest(uri: String): F[Response[F]] =
    routes.run(Request[F](Method.DELETE, Uri.unsafeFromString(uri)))

  private val root = Uri.unsafeFromString("/")

  test("POST: Creating a new TODO") {
    val newTodo = CreateTodoRequest("New Todo", 0)

    for {
      response <- routes.run(Request[F](Method.POST, root).withEntity(newTodo))
      createdTodo <- response.as[TodoItem]
    } yield {
      assertEquals(response.status, Status.Created)
      // Add more assertions as needed
    }
  }

  test("GET: Getting that TODO") {
    val newTodo = CreateTodoRequest("New Todo", 0)

    for {
      createdTodo <- routes.run(Request[F](Method.POST, root).withEntity(newTodo)).as[TodoItem]
      response <- getRequest(s"/${createdTodo.id}")
      fetchedTodo <- response.as[TodoItem]
    } yield {
      assertEquals(response.status, Status.Ok)
      assertEquals(fetchedTodo, createdTodo)
    }
  }

  test("GET: Trying to get a TODO that doesn't exist") {
    val nonExistingId = Long.MaxValue

    for {
      response <- getRequest(s"/$nonExistingId")
    } yield {
      assertEquals(response.status, Status.NotFound)
    }
  }

  test("GET: Getting all todos") {
    val newTodo1 = CreateTodoRequest("Todo 1", 0)
    val newTodo2 = CreateTodoRequest("Todo 2", 1)

    for {
      _ <- routes.run(Request[F](Method.POST, root).withEntity(newTodo1))
      _ <- routes.run(Request[F](Method.POST, root).withEntity(newTodo2))
      response <- getRequest("/")
      todos <- response.as[List[TodoItem]]
    } yield {
      assertEquals(response.status, Status.Ok)
      assertEquals(todos.length, 2)
    }
  }

  test("PATCH: Updating that todo") {
    val newTodo = CreateTodoRequest("New Todo", 0)
    val updateTodo = UpdateTodoRequest(Some("Updated Title"), None, None)

    for {
      postResponse <- routes.run(Request[F](Method.POST, root).withEntity(newTodo))
      createdTodo <- postResponse.as[TodoItem]
      patchResponse <- routes.run(Request[F](Method.PATCH, root).withEntity(updateTodo))
      updatedTodo <- patchResponse.as[TodoItem]
    } yield {
      assertEquals(patchResponse.status, Status.Ok)
      assertEquals(updatedTodo.title, "Updated Title")
    }
  }

  test("PATCH: Trying to update a todo that doesn't exist") {
    val nonExistingId = Long.MaxValue
    val updateTodo = UpdateTodoRequest(Some("Updated Title"), None, None)

    for {
      response <- routes.run(Request[F](Method.PATCH, root).withEntity(updateTodo))
    } yield {
      assertEquals(response.status, Status.NotFound)
    }
  }

  test("DELETE: Trying to delete a non-existing todo") {
    val nonExistingId = Long.MaxValue

    for {
      response <- deleteRequest(s"/$nonExistingId")
    } yield {
      assertEquals(response.status, Status.NotFound)
    }
  }

  test("DELETE all TODOs") {
    val newTodo1 = CreateTodoRequest("Todo 1", 0)
    val newTodo2 = CreateTodoRequest("Todo 2", 1)

    for {
      _ <- routes.run(Request[F](Method.POST, root).withEntity(newTodo1))
      _ <- routes.run(Request[F](Method.POST, root).withEntity(newTodo2))
      deleteResponse <- deleteRequest("/")
      getAllResponse <- getRequest("/")
      todos <- getAllResponse.as[List[TodoItem]]
    } yield {
      assertEquals(deleteResponse.status, Status.Ok)
      assertEquals(todos.length, 0)
    }
  }
}
