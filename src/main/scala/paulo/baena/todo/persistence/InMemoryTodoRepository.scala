package paulo.baena.todo.persistence

import cats.effect.Async
import doobie.Transactor
import paulo.baena.todo.config.DatabaseConfig
import paulo.baena.todo.persistence.Representations.TodoItem

final case class InMemoryTodoRepository[F[_]: Async](transactor: Transactor[F]) extends TodoRepository[F] {
  def createTodo(createTodo: Representations.CreateTodoCommand): F[TodoItem] = ???

  def getTodoById(todoId: Long): F[Option[TodoItem]] = ???

  def getAll: F[List[TodoItem]] = ???

  def deleteTodoById(todoId: Long): F[Option[Unit]] = ???

  def deleteAll: F[Unit] = ???

  def updateTodo(todoId: Long, updateTodo: Representations.UpdateTodoCommand): F[Option[TodoItem]] = ???
}
