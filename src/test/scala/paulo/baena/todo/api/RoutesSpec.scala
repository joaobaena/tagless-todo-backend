package paulo.baena.todo.api

import cats.data.OptionT
import cats.effect.*
import cats.implicits.*
import org.http4s.*
import org.http4s.circe.*
import io.circe.generic.auto.*
import io.circe.{Decoder, Encoder}
import munit.CatsEffectSuite
import paulo.baena.todo.api.Messages.*
import paulo.baena.todo.persistence.Representations.TodoItem
import paulo.baena.todo.persistence.TestTodoRepository

import java.time.OffsetDateTime

class RoutesSpec[F[_]: Async] extends CatsEffectSuite {

  val testRepo: TestTodoRepository[F] = TestTodoRepository.inMemory[F]

  val routes: HttpRoutes[F] = Routes.live(testRepo)

  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]

  implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  def getRequest(uri: String)(implicit F: Sync[F]): F[Response[F]] =
    routes.run(Request[F](Method.GET, Uri.unsafeFromString(uri))).value.flatMap {
      case Some(response) => F.pure(response)
      case None           => F.raiseError(new RuntimeException("Response not found"))
    }

  def deleteRequest(uri: String)(implicit F: Sync[F]): F[Response[F]] =
    routes.run(Request[F](Method.DELETE, Uri.unsafeFromString(uri))).value.flatMap {
      case Some(response) => F.pure(response)
      case None           => F.raiseError(new RuntimeException("Response not found"))
    }

  def getResponse(maybeResponse: OptionT[F, Response[F]])(implicit F: Sync[F]): F[Response[F]] =
    maybeResponse.value.flatMap {
      case Some(response) => F.pure(response)
      case None           => F.raiseError(new RuntimeException("Response not found"))
    }

  private val root = Uri.unsafeFromString("/")

  test("POST: Creating a new TODO") {
    val newTodo = CreateTodoRequest("New Todo", 0)

    for {
      response    <- getResponse(routes.run(Request[F](Method.POST, root).withEntity(newTodo)))
      createdTodo <- response.as[TodoItem]
    } yield assertEquals(response.status, Status.Created)
    // Add more assertions as needed
  }

  test("GET: Trying to get a TODO that doesn't exist") {
    val nonExistingId = Long.MaxValue

    for {
      response <- getRequest(s"/$nonExistingId")
    } yield assertEquals(response.status, Status.NotFound)
  }

  test("GET: Getting all todos") {
    val newTodo1 = CreateTodoRequest("Todo 1", 0)
    val newTodo2 = CreateTodoRequest("Todo 2", 1)

    for {
      _        <- getResponse(routes.run(Request[F](Method.POST, root).withEntity(newTodo1)))
      _        <- getResponse(routes.run(Request[F](Method.POST, root).withEntity(newTodo2)))
      response <- getRequest("/")
      todos    <- response.as[List[TodoItem]]
    } yield {
      assertEquals(response.status, Status.Ok)
      assertEquals(todos.length, 2)
    }
  }

  test("PATCH: Updating that todo") {
    val newTodo    = CreateTodoRequest("New Todo", 0)
    val updateTodo = UpdateTodoRequest(Some("Updated Title"), None, None)

    for {
      postResponse  <- getResponse(routes.run(Request[F](Method.POST, root).withEntity(newTodo)))
      createdTodo   <- postResponse.as[TodoItem]
      patchResponse <- getResponse(routes.run(Request[F](Method.PATCH, root).withEntity(updateTodo)))
      updatedTodo   <- patchResponse.as[TodoItem]
    } yield {
      assertEquals(patchResponse.status, Status.Ok)
      assertEquals(updatedTodo.title, "Updated Title")
    }
  }

  test("PATCH: Trying to update a todo that doesn't exist") {
    val nonExistingId = Long.MaxValue
    val updateTodo    = UpdateTodoRequest(Some("Updated Title"), None, None)

    for {
      response <- getResponse(routes.run(Request[F](Method.PATCH, root).withEntity(updateTodo)))
    } yield assertEquals(response.status, Status.NotFound)
  }

  test("DELETE: Trying to delete a non-existing todo") {
    val nonExistingId = Long.MaxValue

    for {
      response <- deleteRequest(s"/$nonExistingId")
    } yield assertEquals(response.status, Status.NotFound)
  }

  test("DELETE all TODOs") {
    val newTodo1 = CreateTodoRequest("Todo 1", 0)
    val newTodo2 = CreateTodoRequest("Todo 2", 1)

    for {
      _              <- getResponse(routes.run(Request[F](Method.POST, root).withEntity(newTodo1)))
      _              <- getResponse(routes.run(Request[F](Method.POST, root).withEntity(newTodo2)))
      deleteResponse <- deleteRequest("/")
      getAllResponse <- getRequest("/")
      todos          <- getAllResponse.as[List[TodoItem]]
    } yield {
      assertEquals(deleteResponse.status, Status.Ok)
      assertEquals(todos.length, 0)
    }
  }
}
