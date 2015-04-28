package com.codemettle.jsactorexample.web.components

import example.logging.WebLogging
import japgolly.scalacss.ScalaCssReact._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

import com.codemettle.jsactorexample.web.components.Bootstrap.Modal
import com.codemettle.jsactorexample.web.shared.{TodoHigh, TodoItem, TodoLow, TodoNormal}
import com.codemettle.jsactorexample.web.widgets.Button

/**
 * @author steven
 *
 */

object TodoForm extends WebLogging {
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(item: Option[TodoItem], submitHandler: (TodoItem, Boolean) ⇒ Unit)

  case class State(item: TodoItem, cancelled: Boolean = true)

  class Backend(t: BackendScope[Props, State]) {
    def submitForm(): Unit = {
      t.modState(_.copy(cancelled = false))
    }

    def formClosed(): Unit = {
      t.props.submitHandler(t.state.item, t.state.cancelled)
    }

    def updateDescription(e: ReactEventI) = {
      t.modState(s ⇒ s.copy(item = s.item.copy(content = e.currentTarget.value)))
    }

    def updatePriority(e: ReactEventI) = {
      val newPri = e.currentTarget.value match {
        case p if p == TodoHigh.toString ⇒ TodoHigh
        case p if p == TodoNormal.toString ⇒ TodoNormal
        case p if p == TodoLow.toString ⇒ TodoLow
      }

      t.modState(s ⇒ s.copy(item = s.item.copy(priority = newPri)))
    }
  }

  val component = ReactComponentB[Props]("TodoForm")
    .initialStateP(props ⇒ State(props.item getOrElse TodoItem(-1L, "", TodoNormal, completed = false)))
    .backend(new Backend(_))
    .render((P, S, B) ⇒ {
      logger.debug(s"User is ${if (S.item.id < 0) "adding" else "editing"} a todo")

      val headerText = if (S.item.id < 0) "Add new todo" else "Edit todo"

      Modal(Modal.Props(
        header = be ⇒ <.span(<.button(^.tpe := "button", bss.close, ^.onClick --> be.hide(), Icon.close), <.h4(headerText)),
        footer = be ⇒ <.span(Button(_ ⇒ {B.submitForm(); be.hide()})("OK")),
        closed = B.formClosed),

        <.div(bss.formGroup,
          <.label(^.`for` := "description", "Description"),
          <.input(^.tpe := "text", bss.formControl, ^.id := "description", ^.value := S.item.content,
            ^.placeholder := "write description", ^.onChange ==> B.updateDescription)
        ),
        <.div(bss.formGroup,
          <.label(^.`for` := "priority", "Priority"),
          <.select(bss.formControl, ^.id := "priority", ^.value := S.item.priority.toString, ^.onChange ==> B.updatePriority,
            <.option(^.value := TodoHigh.toString, "High"),
            <.option(^.value := TodoNormal.toString, "Normal"),
            <.option(^.value := TodoLow.toString, "Low")
          )
        )
      )
    }).build

  def apply(props: Props) = component(props)
}
