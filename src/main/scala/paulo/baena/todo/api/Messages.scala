package paulo.baena.todo.api

import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}
import io.scalaland.chimney.dsl.*
import paulo.baena.todo.persistence.Representations.{CreateTodoCommand, UpdateTodoCommand}

object Messages {
  final case class CreateTodoRequest(title: String, order: Option[Int]) {
    lazy val asCreateTodoCommand = this.transformInto[CreateTodoCommand]
  }

  object CreateTodoRequest {
    implicit val decoder: Decoder[CreateTodoRequest] = deriveDecoder
  }

  final case class UpdateTodoRequest(title: Option[String], order: Option[Int], completed: Option[Boolean]) {
    lazy val asUpdateTodoCommand = this.transformInto[UpdateTodoCommand]
  }

  object UpdateTodoRequest {
    implicit val decoder: Decoder[UpdateTodoRequest] = deriveDecoder
  }

  final case class TodoItemResponse(id: Long, title: String, order: Option[Int], completed: Boolean, url: String)

  object TodoItemResponse {
    implicit val encoder: Encoder[TodoItemResponse] = deriveEncoder
  }
}
