package paulo.baena.todo.api

import cats.effect.IO
import cats.implicits.*
import org.http4s.*
import io.circe.generic.auto.*
import io.circe.Decoder
import munit.CatsEffectSuite
import paulo.baena.todo.api.Messages.*

class RoutesSpec extends CatsEffectSuite {

  import ApiTestPrimitives._

  private val root = "/"
  
  test("POST: Creating a new TODO") {
    val newTodo = CreateTodoRequest("New Todo", 1.some)

    for {
      response <- postRequest(root, newTodo)
      _        <- response.as[TodoItemResponse]
    } yield assertEquals(response.status, Status.Created)
  }

  test("GET: Trying to get a TODO that doesn't exist") {
    for {
      response <- getRequest(s"/$nonExistingId")
    } yield assertEquals(response.status, Status.NotFound)
  }

  test("GET: Getting all TODOs") {
    val newTodo1 = CreateTodoRequest("Todo 1", None)
    val newTodo2 = CreateTodoRequest("Todo 2", 1.some)
    for {
      _        <- deleteRequest(root)
      _        <- postRequest(root, newTodo1)
      _        <- postRequest(root, newTodo2)
      response <- getRequest(root)
      todos    <- response.as[List[TodoItemResponse]]
    } yield {
      assertEquals(response.status, Status.Ok)
      assertEquals(todos.length, 2)
    }
  }

  test("PATCH: Updating that TODO") {
    val newTodo    = CreateTodoRequest("New Todo", None)
    val updateTodo = UpdateTodoRequest(Some("Updated Title"), None, None)

    for {
      postResponse  <- postRequest(root, newTodo)
      createdTodo   <- postResponse.as[TodoItemResponse]
      patchResponse <- patchRequest(s"/${createdTodo.id}", updateTodo)
      updatedTodo   <- patchResponse.as[TodoItemResponse]
    } yield {
      assertEquals(patchResponse.status, Status.Ok)
      assertEquals(updatedTodo.title, "Updated Title")
    }
  }

  test("PATCH: Trying to update a TODO that doesn't exist") {
    val updateTodo    = UpdateTodoRequest(Some("Updated Title"), None, None)

    for {
      response <- patchRequest(s"/${nonExistingId}", updateTodo)
    } yield assertEquals(response.status, Status.NotFound)
  }

  test("DELETE: Trying to delete a non-existing TODO") {
    for {
      response <- deleteRequest(s"/$nonExistingId")
    } yield assertEquals(response.status, Status.NotFound)
  }

  test("DELETE all TODOs") {
    val newTodo1 = CreateTodoRequest("Todo 1", None)
    val newTodo2 = CreateTodoRequest("Todo 2", 1.some)

    for {
      _              <- postRequest(root, newTodo1)
      _              <- postRequest(root, newTodo2)
      deleteResponse <- deleteRequest(root)
      getAllResponse <- getRequest(root)
      todos          <- getAllResponse.as[List[TodoItemResponse]]
    } yield {
      assertEquals(deleteResponse.status, Status.Ok)
      assertEquals(todos.length, 0)
    }
  }
}
