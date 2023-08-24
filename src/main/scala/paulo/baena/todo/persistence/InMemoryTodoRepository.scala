package paulo.baena.todo.persistence

import cats.effect.Async
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import paulo.baena.todo.persistence.Representations.*

final case class InMemoryTodoRepository[F[_]: Async](transactor: Transactor[F]) extends TodoRepository[F] {
  def createTodo(createTodo: CreateTodoCommand): F[TodoItem] =
    sql"""
         INSERT INTO todos (title, order, completed)
         VALUES (${createTodo.title}, ${createTodo.order}, FALSE)
         RETURNING id, title, order, completed, updated_at, created_at
       """
      .query[TodoItem]
      .unique
      .transact(transactor)

  def getTodoById(todoId: Long): F[Option[TodoItem]] =
    sql"""
         SELECT id, title, order, completed, updated_at, created_at
         FROM todos
         WHERE id = $todoId
         """
      .query[TodoItem]
      .option
      .transact(transactor)

  def getAll: F[List[TodoItem]] =
    sql"""
         SELECT id, title, order, completed, updated_at, created_at
         FROM todos
         """
      .query[TodoItem]
      .to[List]
      .transact(transactor)

  def deleteTodoById(todoId: Long): F[Option[Unit]] =
    sql"""
         DELETE FROM todos
         WHERE id = $todoId
         """.update.run
      .transact(transactor)
      .map {
        case 0 => None
        case _ => ().some
      }

  def deleteAll: F[Unit] =
    sql"""
         DELETE FROM todos
         """.update.run.void
      .transact(transactor)

  def updateTodo(todoId: Long, updateTodo: UpdateTodoCommand): F[Option[TodoItem]] =
    sql"""
         UPDATE todos
         SET (title = ${updateTodo.title}, order = ${updateTodo.order}, completed = ${updateTodo.completed})
         WHERE id = $todoId
         RETURNING id, title, order, completed, updated_at, created_at
       """
      .query[TodoItem]
      .option
      .transact(transactor)
}
