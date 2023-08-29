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
    itemOrder: Option[Int],
    completed: Boolean,
    updatedAt: OffsetDateTime,
    createdAt: OffsetDateTime
  ) {
    def asTodoItemResponse(implicit appUrl: String) = this
      .into[TodoItemResponse]
      .withFieldRenamed(_.itemOrder, _.order)
      .withFieldComputed(_.url, todoItem => s"$appUrl/${todoItem.id}")
      .transform
  }

  implicit val offsetDateTimeMeta: Meta[OffsetDateTime] =
    Meta[java.sql.Timestamp].imap(ts => ts.toInstant.atOffset(OffsetDateTime.now().getOffset))(odt =>
      java.sql.Timestamp.from(odt.toInstant)
    )

  implicit val todoItemRead: Read[TodoItem] =
    Read[(Long, String, Option[Int], Boolean, OffsetDateTime, OffsetDateTime)].map {
      case (id, title, order, completed, updatedAt, createdAt) =>
        TodoItem(id, title, order, completed, updatedAt, createdAt)
    }

  final case class CreateTodoCommand(title: String, order: Option[Int])

  final case class UpdateTodoCommand(title: Option[String], order: Option[Int], completed: Option[Boolean])

}
