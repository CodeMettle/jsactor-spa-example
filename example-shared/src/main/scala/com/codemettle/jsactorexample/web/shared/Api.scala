package com.codemettle.jsactorexample.web.shared

/**
 * @author steven
 *
 */
object Api {
  case class GetMotd(name: String)
  case class GotMotd(motd: String)

  case object SubscribeToTodos
  case class TodosChanged(todos: Map[Long, TodoItem])
  case class UpdateTodo(todo: TodoItem)
  case class DeleteTodo(todoId: Long)
  case class TodoDeleted(todoId: Long)
}
