package com.codemettle.jsactorexample.web.services

import java.util.Date

import com.codemettle.jsactorexample.web.shared.Api._
import com.codemettle.jsactorexample.web.shared.{TodoHigh, TodoItem, TodoLow, TodoNormal}

import akka.actor._
import scala.concurrent.duration._

/**
 * @author steven
 *
 */
object SPAService {
  val props = Props(new SPAService)
}

class SPAService extends Actor with ActorLogging {
  import context.dispatcher

  private var todos = Map(
    1L → TodoItem(1, "Wear shirt that says “Life”. Hand out lemons on street corner.", TodoLow, completed = false),
    2L → TodoItem(2, "Make vanilla pudding. Put in mayo jar. Eat in public.", TodoNormal, completed = false),
    3L → TodoItem(3, "Walk away slowly from an explosion without looking back.", TodoHigh, completed = false),
    4L → TodoItem(4, "Sneeze in front of the pope. Get blessed.", TodoNormal, completed = true)
  )

  private var subscribers = Set.empty[ActorRef]

  log.info("{} started", self.path)

  def receive = {
    case SubscribeToTodos ⇒
      context watch sender()
      subscribers += sender()
      sender() ! TodosChanged(todos)

    case Terminated(act) ⇒ subscribers -= act

    case UpdateTodo(todo) ⇒
      def nextKey = todos.keys.toList match {
        case x :: xs ⇒ (x :: xs).max + 1L
        case Nil ⇒ 1L
      }

      val toAdd = if (todos contains todo.id) todo else todo.copy(id = nextKey)

      todos += (toAdd.id → toAdd)
      val msg = TodosChanged(Map(toAdd.id → toAdd))
      subscribers foreach (_ ! msg)

    case DeleteTodo(todoId) ⇒
      if (todos contains todoId) {
        todos -= todoId
        val msg = TodoDeleted(todoId)
        subscribers foreach (_ ! msg)
      }

    case GetMotd(name) ⇒
      val sendTo = sender()
      context.system.scheduler.scheduleOnce(1.second) {
        sendTo ! GotMotd(s"Welcome to SPA, $name! Time is now ${new Date}")
      }
  }
}
