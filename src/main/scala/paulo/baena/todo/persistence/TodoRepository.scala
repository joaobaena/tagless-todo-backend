package paulo.baena.todo.persistence

import paulo.baena.todo.persistence.Representations._

trait TodoRepository[F[_]] {
  def createTodo(createTodo: CreateTodo): F[TodoItem]
  def getTodoById(todoId: Long): F[Option[TodoItem]]
  def getAll: F[List[TodoItem]]
  def deleteTodoById(todoId: Long): F[Unit]
  def deleteAll: F[Unit]
  def update(todoId: Long, updateTodo: UpdateTodo): F[TodoItem]
}
