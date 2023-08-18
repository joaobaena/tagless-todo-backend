package paulo.baena.todo.persistence

import paulo.baena.todo.persistence.Representations._

trait TodoRepository[F[_]] {
  def createTodo(createTodo: CreateTodoCommand): F[TodoItem]
  def getTodoById(todoId: Long): F[Option[TodoItem]]
  def getAll: F[List[TodoItem]]
  def deleteTodoById(todoId: Long): F[Option[Unit]]
  def deleteAll: F[Unit]
  def updateTodo(todoId: Long, updateTodo: UpdateTodoCommand): F[Option[TodoItem]]
}
