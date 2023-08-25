package paulo.baena.todo.persistence

import doobie._
import doobie.implicits.javasql.TimestampMeta
import io.scalaland.chimney.dsl.*
import paulo.baena.todo.api.Messages.TodoItemResponse

import java.time.OffsetDateTime

object Representations {
  final case class TodoItem(
    id: Long,
    title: String,
    itemOrder: Int,
    completed: Boolean,
    updatedAt: OffsetDateTime,
    createdAt: OffsetDateTime
  ) {
    lazy val asTodoItemResponse = this
      .into[TodoItemResponse]
      .withFieldRenamed(_.itemOrder, _.order)
      .transform
  }

  // TODO: doobie type classes should live here or on the repo?
  implicit val offsetDateTimeMeta: Meta[OffsetDateTime] =
    Meta[java.sql.Timestamp].imap(ts => ts.toInstant.atOffset(OffsetDateTime.now().getOffset))(odt =>
      java.sql.Timestamp.from(odt.toInstant)
    )

  implicit val todoItemRead: Read[TodoItem] = Read[(Long, String, Int, Boolean, OffsetDateTime, OffsetDateTime)].map {
    case (id, title, order, completed, updatedAt, createdAt) =>
      TodoItem(id, title, order, completed, updatedAt, createdAt)
  }

  final case class CreateTodoCommand(title: String, order: Int)

  final case class UpdateTodoCommand(title: Option[String], order: Option[Int], completed: Option[Boolean])

}
