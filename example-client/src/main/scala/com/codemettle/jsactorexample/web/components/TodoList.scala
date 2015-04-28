package com.codemettle.jsactorexample.web.components

import japgolly.scalacss.ScalaCssReact._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

import com.codemettle.jsactorexample.web.components.Bootstrap.CommonStyle
import com.codemettle.jsactorexample.web.shared.{TodoHigh, TodoItem, TodoLow, TodoNormal}
import com.codemettle.jsactorexample.web.widgets.Button

/**
 * @author steven
 *
 */
object TodoList {
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(items: Seq[TodoItem], stateChange: (TodoItem) ⇒ Unit, editItem: (TodoItem) ⇒ Unit, deleteItem: (TodoItem) ⇒ Unit)

  val TodoList = ReactComponentB[Props]("TodoList").render(P ⇒ {
    val style = bss.listGroup

    def renderItem(item: TodoItem) = {
      val itemStyle = item.priority match {
        case TodoLow ⇒ style.itemOpt(CommonStyle.info)
        case TodoNormal ⇒ style.item
        case TodoHigh ⇒ style.itemOpt(CommonStyle.danger)
      }

      <.li(itemStyle)(
        <.input(^.tpe := "checkbox", ^.checked := item.completed, ^.onChange --> P.stateChange(item.copy(completed = !item.completed))),
        <.span(" "),
        if (item.completed) <.s(item.content) else <.span(item.content),
        Button(_ ⇒ P.editItem(item), size = Button.Size.xs, addl = Seq(^.className := "pull-right"))("Edit"),
        Button(_ ⇒ P.deleteItem(item), size = Button.Size.xs, addl = Seq(^.className := "pull-right"))("Delete")
      )
    }

    <.ul(style.listGroup)(P.items map renderItem)
  }).build

  def apply(items: Seq[TodoItem], stateChange: (TodoItem) ⇒ Unit, editItem: (TodoItem) ⇒ Unit, deleteItem: (TodoItem) ⇒ Unit) = {
    TodoList(Props(items, stateChange, editItem, deleteItem))
  }
}
