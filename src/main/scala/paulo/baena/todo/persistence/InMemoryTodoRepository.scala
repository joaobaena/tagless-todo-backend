package paulo.baena.todo.persistence

import cats.effect._
import cats.implicits.*
import doobie.*
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import paulo.baena.todo.persistence.Representations.*

final case class InMemoryTodoRepository[F[_]](transactor: Transactor[F])(implicit F: Sync[F])
    extends TodoRepository[F] {
  def createTodo(createTodo: CreateTodoCommand): F[TodoItem] =
    sql"""
         INSERT INTO todos (title, item_order, completed)
         VALUES (${createTodo.title}, ${createTodo.order}, FALSE)
         RETURNING id, title, item_order, completed, updated_at, created_at
       """
      .query[TodoItem]
      .unique
      .transact(transactor)

  def getTodoById(todoId: Long): F[Option[TodoItem]] =
    sql"""
         SELECT id, title, item_order, completed, updated_at, created_at
         FROM todos
         WHERE id = $todoId
         """
      .query[TodoItem]
      .option
      .transact(transactor)

  def getAll: F[List[TodoItem]] =
    for {
      _   <- Sync[F].delay(println(s"starting query with transactor: ${transactor}"))
      res <- sql"""
         SELECT id, title, item_order, completed, updated_at, created_at
         FROM todos
         """
               .query[TodoItem]
               .to[List]
               .transact(transactor)
      _   <- Sync[F].delay(println(s"finished"))

    } yield res

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
         SET (title = ${updateTodo.title}, item_order = ${updateTodo.order}, completed = ${updateTodo.completed}, updated_at = CURRENT_TIMESTAMP)
         WHERE id = $todoId
         RETURNING id, title, item_order, completed, updated_at, created_at
       """
      .query[TodoItem]
      .option
      .transact(transactor)
}

object InMemoryTodoRepository {
  def live[F[_]: Sync](transactor: Resource[F, HikariTransactor[F]]): Resource[F, InMemoryTodoRepository[F]] =
    for {
      xa <- transactor
    } yield InMemoryTodoRepository[F](xa)
}
