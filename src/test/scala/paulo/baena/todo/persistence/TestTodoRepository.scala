package paulo.baena.todo.persistence

import cats.effect.*
import cats.effect.Ref
import cats.implicits.*
import paulo.baena.todo.persistence.Representations.*

import java.time.{Instant, OffsetDateTime, ZoneOffset}

final case class TestTodoRepository[F[_]: Sync](
  clock: Clock[F],
  ref: Ref[F, Map[Long, TodoItem]],
  idCounter: Ref[F, Long]
) extends TodoRepository[F] {

  def createTodo(createTodo: CreateTodoCommand): F[TodoItem] =
    for {
      newId      <- idCounter.modify(id => (id + 1, id))
      now        <- getOffsetDateTime
      createdTodo = TodoItem(newId, createTodo.title, createTodo.order, false, now, now)
      _          <- ref.update(_ + (newId -> createdTodo))
    } yield createdTodo

  def getTodoById(todoId: Long): F[Option[TodoItem]] =
    ref.get.map(_.get(todoId))

  def getAll: F[List[TodoItem]] =
    ref.get.map(_.values.toList)

  def updateTodo(todoId: Long, updateTodo: UpdateTodoCommand): F[Option[TodoItem]] =
    for {
      maybeTodo        <- getTodoById(todoId)
      maybeUpdatedTodo <- maybeTodo.traverse(applyUpdateTodo(updateTodo, _))
      _                <- maybeUpdatedTodo.traverse(updatedTodo => ref.update(_.updated(updatedTodo.id, updatedTodo)))
    } yield maybeUpdatedTodo

  private def applyUpdateTodo(updateTodo: UpdateTodoCommand, originalTodo: TodoItem): F[TodoItem] =
    getOffsetDateTime.map(now =>
      originalTodo
        .copy(
          title = updateTodo.title.getOrElse(originalTodo.title),
          itemOrder = updateTodo.order.fold(originalTodo.itemOrder)(_.some),
          completed = updateTodo.completed.getOrElse(originalTodo.completed),
          updatedAt = now
        )
    )

  def deleteTodoById(todoId: Long): F[Option[Unit]] =
    ref.modify { todos =>
      if (todos.contains(todoId)) (todos - todoId, Some(()))
      else (todos, None)
    }

  def deleteAll: F[Unit] =
    ref.update(_ => Map.empty)

  private def getOffsetDateTime: F[OffsetDateTime] =
    for {
      now    <- clock.realTime
      nowUTC <- Sync[F].delay(OffsetDateTime.ofInstant(Instant.ofEpochSecond(0, now.toNanos), ZoneOffset.UTC))
    } yield nowUTC
}

object TestTodoRepository {
  def inMemory[F[_]: Sync]: F[TestTodoRepository[F]] =
    for {
      ref       <- Ref.of[F, Map[Long, TodoItem]](Map.empty)
      idCounter <- Ref.of[F, Long](1L)
    } yield TestTodoRepository(Clock[F], ref, idCounter)
}
