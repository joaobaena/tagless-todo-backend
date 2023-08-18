package paulo.baena.todo.api

import cats.effect.Async
import cats.implicits.*
import io.circe.{Decoder, Encoder}
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import paulo.baena.todo.api.Messages.{CreateTodoRequest, UpdateTodoRequest}
import paulo.baena.todo.persistence.TodoRepository

trait Routes[F[_]: Async] {
  private val dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl._

  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]

  implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  def httpRoutes(todoRepository: TodoRepository[F]): HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      for {
        todoItems        <- todoRepository.getAll
        todoItemsResponse = todoItems.map(_.asTodoItemResponse)
        response         <- Ok(todoItemsResponse)
      } yield response

    case GET -> Root / LongVar(todoId) =>
      for {
        maybeTodoItem <- todoRepository.getTodoById(todoId)
        response      <- maybeTodoItem.fold(NotFound(s"Id $todoId not found"))(todoItem => Ok(todoItem.asTodoItemResponse))
      } yield response

    case request @ POST -> Root =>
      request.decode[CreateTodoRequest] { createTodoRequest =>
        for {
          createdTodoItem <- todoRepository.createTodo(createTodoRequest.asCreateTodoCommand)
          response        <- Created(createdTodoItem.asTodoItemResponse)
        } yield response
      }

    case request @ PATCH -> Root / LongVar(todoId) =>
      request.decode[UpdateTodoRequest] { updateTodoRequest =>
        for {
          maybeTodoItem <- todoRepository.updateTodo(todoId, updateTodoRequest.asUpdateTodoCommand)
          response      <- maybeTodoItem.fold(NotFound(s"Id $todoId not found"))(todoItem => Ok(todoItem.asTodoItemResponse))
        } yield response
      }

    case DELETE -> Root =>
      for {
        _        <- todoRepository.deleteAll
        response <- Ok("Deleted")
      } yield response

    case DELETE -> Root / LongVar(todoId) =>
      for {
        maybeDeleted <- todoRepository.deleteTodoById(todoId)
        response     <- maybeDeleted.fold(NotFound(s"Id $todoId not found"))(_ => Ok("Deleted"))
      } yield response
  }
}