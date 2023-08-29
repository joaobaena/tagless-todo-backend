package paulo.baena.todo.api

import cats.effect.Async
import cats.implicits.*
import io.circe.{Decoder, Encoder}
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.{CORS, ErrorAction}
import org.typelevel.log4cats.LoggerFactory
import paulo.baena.todo.api.Messages.{CreateTodoRequest, UpdateTodoRequest}
import paulo.baena.todo.persistence.TodoRepository

trait Routes[F[_]: Async] {
  private val dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl._

  // TODO: move to own class so it doesn't polute the routes
  private object CirceCodec {
    implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]

    implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[A]

  }

  def httpRoutes(todoRepository: TodoRepository[F])(implicit appUrl: String): HttpRoutes[F] = {
    import CirceCodec._

    HttpRoutes.of[F] {
      case GET -> Root / basePath =>
        for {
          todoItems        <- todoRepository.getAll
          todoItemsResponse = todoItems.map(_.asTodoItemResponse)
          response         <- Ok(todoItemsResponse)
        } yield response

      case GET -> Root / basePath / LongVar(todoId) =>
        for {
          maybeTodoItem <- todoRepository.getTodoById(todoId)
          response      <- maybeTodoItem.fold(NotFound(s"Id $todoId not found"))(todoItem => Ok(todoItem.asTodoItemResponse))
        } yield response

      case request @ POST -> Root / basePath =>
        for {
          createTodoRequest <- request.as[CreateTodoRequest]
          createdTodoItem   <- todoRepository.createTodo(createTodoRequest.asCreateTodoCommand)
          response          <- Created(createdTodoItem.asTodoItemResponse)
        } yield response

      case request @ PATCH -> Root / basePath / LongVar(todoId) =>
        for {
          updateTodoRequest <- request.as[UpdateTodoRequest]
          maybeTodoItem     <- todoRepository.updateTodo(todoId, updateTodoRequest.asUpdateTodoCommand)
          response          <-
            maybeTodoItem.fold(NotFound(s"Id $todoId not found"))(todoItem => Ok(todoItem.asTodoItemResponse))
        } yield response

      case DELETE -> Root / basePath =>
        for {
          _        <- todoRepository.deleteAll
          response <- Ok("Deleted")
        } yield response

      case DELETE -> Root / basePath / LongVar(todoId) =>
        for {
          maybeDeleted <- todoRepository.deleteTodoById(todoId)
          response     <- maybeDeleted.fold(NotFound(s"Id $todoId not found"))(_ => Ok("Deleted"))
        } yield response
    }
  }
}

object Routes {
  def live[F[_]: Async](todoRepository: TodoRepository[F], appUrl: String)(implicit
    loggerFactory: LoggerFactory[F]
  ): F[HttpRoutes[F]] = {
    val httpRoutes = new Routes[F] {}.httpRoutes(todoRepository)(appUrl)
    val logger     = loggerFactory.getLogger
    // TODO: can the cors policy be update so it only allows CORS from todobackend.com?
    CORS.policy
      .withAllowOriginAll(httpRoutes)
      .map(service =>
        // TODO: also it would be good to have a handler that mapped error messages into the 500 response
        ErrorAction
          .httpRoutes[F](service, (_, thr) => logger.error("Error: " ++ thr.getMessage))
      )
  }
  val basePath = "todos"
}
