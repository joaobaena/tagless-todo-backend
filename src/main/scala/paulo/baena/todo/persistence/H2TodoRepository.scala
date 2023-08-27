package paulo.baena.todo.persistence

import cats.effect.*
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.ConnectionIO
import paulo.baena.todo.persistence.Representations.*

final case class H2TodoRepository[F[_]](transactor: Transactor[F])(implicit F: Sync[F]) extends TodoRepository[F] {

  import paulo.baena.todo.persistence.H2TodoRepository.Queries

  def createTodo(createTodo: CreateTodoCommand): F[TodoItem] =
    Queries
      .createTodo(createTodo)
      .withUniqueGeneratedKeys[TodoItem]("id", "title", "item_order", "completed", "updated_at", "created_at")
      .transact(transactor)

  def getTodoById(todoId: Long): F[Option[TodoItem]] =
    Queries
      .getTodoById(todoId)
      .option
      .transact(transactor)

  def getAll: F[List[TodoItem]] =
    Queries.getAll
      .to[List]
      .transact(transactor)

  def deleteTodoById(todoId: Long): F[Option[Unit]] =
    Queries
      .deleteTodoById(todoId: Long)
      .run
      .transact(transactor)
      .map {
        case 0 => None
        case _ => ().some
      }

  def deleteAll: F[Unit] =
    Queries.deleteAll.run.void
      .transact(transactor)

  def updateTodo(todoId: Long, updateTodo: UpdateTodoCommand): F[Option[TodoItem]] =
    Queries
      .getTodoById(todoId)
      .option
      .flatMap {
        case Some(_) =>
          Queries
            .updateTodo(todoId, updateTodo)
            .withUniqueGeneratedKeys[TodoItem]("id", "title", "item_order", "completed", "updated_at", "created_at")
            .map(Some(_))
        case None    =>
          none.pure[ConnectionIO]
      }
      .transact(transactor)
}

object H2TodoRepository {
  object Queries {
    def createTodo(createTodo: CreateTodoCommand): Update0 =
      sql"""
         INSERT INTO todos (title, item_order, completed)
         VALUES (${createTodo.title}, ${createTodo.order}, FALSE)
      """.update

    def getTodoById(todoId: Long): Query0[TodoItem] =
      sql"""
        SELECT id, title, item_order, completed, updated_at, created_at
        FROM todos
        WHERE id = $todoId
      """.query[TodoItem]

    def getAll: Query0[TodoItem] =
      sql"""
        SELECT id, title, item_order, completed, updated_at, created_at
        FROM todos
      """.query[TodoItem]

    def deleteTodoById(todoId: Long): Update0 =
      sql"""
        DELETE FROM todos
        WHERE id = $todoId
      """.update

    def deleteAll: Update0 =
      sql"""
        DELETE FROM todos
      """.update

    def updateTodo(todoId: Long, updateTodo: UpdateTodoCommand): Update0 =
      sql"""
        UPDATE todos
        SET title = ${updateTodo.title}, item_order = ${updateTodo.order}, completed = ${updateTodo.completed}, updated_at = CURRENT_TIMESTAMP
        WHERE id = $todoId
      """.update
  }
}
