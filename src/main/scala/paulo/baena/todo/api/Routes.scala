package paulo.baena.todo.api

import cats.effect.Async
import cats.implicits.*
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.{CORS, ErrorAction}
import org.typelevel.log4cats.LoggerFactory
import paulo.baena.todo.api.Messages.{CreateTodoRequest, UpdateTodoRequest}
import paulo.baena.todo.persistence.TodoRepository

trait Routes[F[_]: Async] extends CirceCodec[F] {
  private val dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl._

  def httpRoutes(todoRepository: TodoRepository[F])(implicit appUrl: String): HttpRoutes[F] =
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

object Routes {
  def live[F[_]: Async](todoRepository: TodoRepository[F], appUrl: String)(implicit
    loggerFactory: LoggerFactory[F]
  ): F[HttpRoutes[F]] = {
    val httpRoutes = new Routes[F] {}.httpRoutes(todoRepository)(appUrl)
    val logger     = loggerFactory.getLogger
    CORS.policy
      .withAllowOriginAll(httpRoutes)
      .map(service =>
        ErrorAction
          .httpRoutes[F](service, (_, thr) => logger.error("Error: " ++ thr.getMessage))
      )
  }
  val basePath = "todos"
}
