package com.codemettle.jsactorexample.web.modules

import example.logging.WebLogging
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.prefix_<^._
import rx._
import rx.ops._

import com.codemettle.jsactorexample.web.components.Bootstrap.Panel
import com.codemettle.jsactorexample.web.components.{Icon, TodoForm, TodoList}
import com.codemettle.jsactorexample.web.services.{Application, TodoStore}
import com.codemettle.jsactorexample.web.shared.TodoItem
import com.codemettle.jsactorexample.web.widgets.Button

/**
 * @author steven
 *
 */
object Todo {
  case class Props(todos: Rx[Seq[TodoItem]], router: MainRouter.Router)

  case class State(selectedItem: Option[TodoItem] = None, showTodoForm: Boolean = false)

  abstract class RxObserver[BS <: BackendScope[_, _]](scope: BS) extends OnUnmount {
    protected def observe[T](rx: Rx[T]): Unit = {
      val obs = rx foreach (_ ⇒ scope.forceUpdate())

      onUnmount(obs.kill())
    }
  }

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) with WebLogging {
    def mounted(): Unit = {
      observe(t.props.todos)
    }

    def editTodo(item: Option[TodoItem]): Unit = {
      t.modState(_.copy(selectedItem = item, showTodoForm = true))
    }

    def deleteTodo(item: TodoItem): Unit = {
      Application.todoStore ! TodoStore.Messages.DeleteTodo(item)
    }

    def todoEdited(item: TodoItem, cancelled: Boolean): Unit = {
      if (cancelled) {
        logger.debug("Todo editing cancelled")
      } else {
        logger.debug(s"Todo edited: $item")
        Application.todoStore ! TodoStore.Messages.UpdateTodo(item)
      }

      t.modState(_.copy(showTodoForm = false))
    }
  }

  val component = ReactComponentB[Props]("ToDo")
    .initialState(State())
    .backend(new Backend(_))
    .render((P, S, B) ⇒ {
      Panel(Panel.Props("What needs to be done"))(
        TodoList(P.todos(), ti ⇒ Application.todoStore ! TodoStore.Messages.UpdateTodo(ti), item ⇒ B.editTodo(Some(item)), B.deleteTodo),
        Button(_ ⇒ B.editTodo(None))(Icon.plusSquare, " New"),
        if (S.showTodoForm) TodoForm(TodoForm.Props(S.selectedItem, B.todoEdited))
        else Seq.empty[ReactElement]
      )
    })
    .componentDidMount(_.backend.mounted())
    .configure(OnUnmount.install)
    .build

  def apply() = (router: MainRouter.Router) ⇒ {
    component(Props(TodoStore.todos, router))
  }
}

case class ReactTodoItem(itemText: String, index: Int)

object ReactTodoList {
  case class Props(items: Seq[ReactTodoItem] = Seq.empty, itemDeleted: (Int) ⇒ Unit)

  val component = ReactComponentB[Props]("ReactTodoList")
    .stateless
    .noBackend
    .render((P, _, _) ⇒ {
      def createItem(item: ReactTodoItem) = {
        <.li(^.key := s"${item.index}${item.itemText}")(
          Button(_ ⇒ P.itemDeleted(item.index), Button.Style.danger, Button.Size.xs)(Icon.minusCircle),
          <.span(" ", item.itemText)
        )
      }

      <.ol(P.items map createItem)
    })
    .build

  def apply(items: Seq[String], itemDeleted: (Int) ⇒ Unit) = {
    component(Props(items.zipWithIndex map ReactTodoItem.tupled, itemDeleted))
  }
}

object ReactTodoApp {
  case class State(items: Seq[String], text: String)

  class Backend(t: BackendScope[Unit, State]) {
    def onChange(e: ReactEventI) = {
      t.modState(_.copy(text = e.target.value))
    }

    def onDeleteClicked(idx: Int): Unit = {
      t.modState(s ⇒ {
        val (f, b) = s.items splitAt idx
        val newItems = if (b.size > 1)
          f ++ b.tail
        else
          f

        s.copy(items = newItems)
      })
    }

    def handleSubmit(e: ReactEvent) = {
      e.preventDefault()
      val nextItems = t.state.items :+ t.state.text
      val nextText = ""
      t.modState(_.copy(items = nextItems, text = nextText))
    }

    def handleClick() = {
      val nextItems = t.state.items :+ t.state.text
      val nextText = ""
      t.modState(_.copy(items = nextItems, text = nextText))
    }
  }

  val component = ReactComponentB[Unit]("ReactTodoApp")
    .initialState(State(Seq.empty, ""))
    .backend(new Backend(_))
    .render((_, S, B) ⇒ {
      <.div(
        <.h3("TODO (from React docs, no backend communication)"),
        ReactTodoList(S.items, B.onDeleteClicked),
        <.div(
          <.input(^.onChange ==> B.onChange, ^.value := S.text, ^.placeholder := "Add a Todo"),
          Button(_ ⇒ B.handleClick(), Button.Style.success, Button.Size.sm)(s"Add #${S.items.size + 1}")
        ),
        <.form(^.onSubmit ==> B.handleSubmit)(
          <.input(^.onChange ==> B.onChange, ^.value := S.text),
          <.button(s"Add #${S.items.size + 1}")
        )
      )
    })
    .buildU

  def apply() = (router: MainRouter.Router) ⇒ component()
}
