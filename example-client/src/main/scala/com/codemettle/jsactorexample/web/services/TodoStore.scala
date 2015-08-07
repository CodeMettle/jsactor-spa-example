package com.codemettle.jsactorexample.web.services

import jsactor.bridge.client.WebSocketManager
import jsactor.bridge.client.util.RemoteActorListener
import jsactor.{JsActorRef, JsProps}
import rx._

import com.codemettle.jsactorexample.web.services.TodoStore.Messages.{DeleteTodo, UpdateTodo}
import com.codemettle.jsactorexample.web.services.TodoStore._items
import com.codemettle.jsactorexample.web.shared.{Api, TodoItem}

/**
 * @author steven
 *
 */
object TodoStore {
  def props(wsManager: WebSocketManager) = {
    JsProps(new TodoStore(wsManager))
  }

  object Messages {
    case class UpdateTodo(item: TodoItem)
    case class DeleteTodo(item: TodoItem)
  }

  private case class UpdatedTodos(todos: Seq[TodoItem])

  private val _items = Var(Seq.empty[TodoItem])
  def todos: Rx[Seq[TodoItem]] = _items
}

class TodoStore(wsManager: WebSocketManager) extends RemoteActorListener {
  override def actorPath = "/user/SPAService"

  override def webSocketManager: WebSocketManager = wsManager

  override def onConnect(serverActor: JsActorRef): Unit = {
    serverActor ! Api.SubscribeToTodos
  }

  override def whenConnected(serverActor: JsActorRef): Receive = {
    case gm: Api.GetMotd ⇒ serverActor forward gm

    case Api.TodosChanged(todos) ⇒
      val newItems = (_items() map (i ⇒ i.id → i)).toMap ++ todos

      _items() = newItems.toSeq sortBy (_._1) map (_._2)

    case Api.TodoDeleted(todoId) ⇒
      val newItems = (_items() map (i ⇒ i.id → i)).toMap - todoId

      _items() = newItems.toSeq sortBy (_._1) map (_._2)

    case UpdateTodo(item) ⇒ serverActor ! Api.UpdateTodo(item)

    case DeleteTodo(item) ⇒ serverActor ! Api.DeleteTodo(item.id)
  }
}
