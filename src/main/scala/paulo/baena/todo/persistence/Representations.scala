package paulo.baena.todo.persistence

import io.scalaland.chimney.dsl.*
import paulo.baena.todo.api.Messages.TodoItemResponse

import java.time.OffsetDateTime

object Representations {
  final case class TodoItem(
    id: Long,
    title: String,
    order: Int,
    completed: Boolean,
    updatedAt: OffsetDateTime,
    createdAt: OffsetDateTime
  ) {
    lazy val asTodoItemResponse = this.transformInto[TodoItemResponse]
  }

  final case class CreateTodoCommand(title: String, order: Int)

  final case class UpdateTodoCommand(title: Option[String], order: Option[Int], completed: Option[Boolean])

}
