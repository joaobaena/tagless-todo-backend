package paulo.baena.todo.persistence

import java.time.OffsetDateTime

object Representations {
  final case class TodoItem(
    id: Long,
    title: String,
    order: Int,
    completed: Boolean,
    updatedAt: OffsetDateTime,
    createdAt: OffsetDateTime
  )

  final case class CreateTodo(title: String, order: Int)

  final case class UpdateTodo(title: Option[String], order: Int, completed: Option[Boolean])

}
