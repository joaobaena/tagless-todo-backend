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

  private val basePath = s"/${Routes.basePath}"

  test("POST: Creating and getting a TODO") {
    val newTodo = CreateTodoRequest("New Todo", 1.some)

    for {
      response           <- postRequest(basePath, newTodo)
      todoItemResponse   <- response.as[TodoItemResponse]
      getResponse        <- getRequest(s"$basePath/${todoItemResponse.id}")
      storedItemResponse <- getResponse.as[TodoItemResponse]
    } yield {
      assertEquals(response.status, Status.Created)
      assertEquals(todoItemResponse, storedItemResponse)
    }
  }

  test("GET: Trying to get a TODO that doesn't exist") {
    for {
      response <- getRequest(s"$basePath/$nonExistingId")
    } yield assertEquals(response.status, Status.NotFound)
  }

  test("GET: Getting all TODOs") {
    val newTodo1 = CreateTodoRequest("Todo 1", None)
    val newTodo2 = CreateTodoRequest("Todo 2", 1.some)
    for {
      _        <- deleteRequest(basePath)
      _        <- postRequest(basePath, newTodo1)
      _        <- postRequest(basePath, newTodo2)
      response <- getRequest(basePath)
      todos    <- response.as[List[TodoItemResponse]]
    } yield {
      assertEquals(response.status, Status.Ok)
      assertEquals(todos.length, 2)
    }
  }

  test("PATCH: Updating a TODO") {
    val newTodo    = CreateTodoRequest("New Todo", None)
    val updateTodo = UpdateTodoRequest(Some("Updated Title"), None, None)

    for {
      postResponse  <- postRequest(basePath, newTodo)
      createdTodo   <- postResponse.as[TodoItemResponse]
      patchResponse <- patchRequest(s"$basePath/${createdTodo.id}", updateTodo)
      updatedTodo   <- patchResponse.as[TodoItemResponse]
    } yield {
      assertEquals(patchResponse.status, Status.Ok)
      assertEquals(updatedTodo.title, "Updated Title")
    }
  }

  test("PATCH: Trying to update a TODO that doesn't exist") {
    val updateTodo = UpdateTodoRequest(Some("Updated Title"), None, None)

    for {
      response <- patchRequest(s"$basePath/${nonExistingId}", updateTodo)
    } yield assertEquals(response.status, Status.NotFound)
  }

  test("DELETE: Trying to delete a non-existing TODO") {
    for {
      response <- deleteRequest(s"$basePath/$nonExistingId")
    } yield assertEquals(response.status, Status.NotFound)
  }

  test("DELETE: all TODOs") {
    val newTodo1 = CreateTodoRequest("Todo 1", None)
    val newTodo2 = CreateTodoRequest("Todo 2", 1.some)

    for {
      _              <- postRequest(basePath, newTodo1)
      _              <- postRequest(basePath, newTodo2)
      deleteResponse <- deleteRequest(basePath)
      getAllResponse <- getRequest(basePath)
      todos          <- getAllResponse.as[List[TodoItemResponse]]
    } yield {
      assertEquals(deleteResponse.status, Status.Ok)
      assertEquals(todos.length, 0)
    }
  }
}
