package paulo.baena.todo.api

object Messages {
  final case class CreateTodoRequest(title: String, order: Int)

  final case class UpdateTodoRequest(title: Option[String], order: Option[Int], completed: Option[Boolean])
}
