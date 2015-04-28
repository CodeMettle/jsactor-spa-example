package com.codemettle.jsactorexample.web.modules

import japgolly.scalacss.ScalaCssReact._
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB, ReactNode, _}
import rx._
import rx.ops._

import com.codemettle.jsactorexample.web.components.Bootstrap.CommonStyle
import com.codemettle.jsactorexample.web.components.Icon.Icon
import com.codemettle.jsactorexample.web.components.{GlobalStyles, Icon}
import com.codemettle.jsactorexample.web.shared.TodoItem

/**
 * @author steven
 *
 */
object MainMenu {
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(activeLocation: MainRouter.Loc, todos: Rx[Seq[TodoItem]])

  case class MenuItem(label: (Props) ⇒ ReactNode, icon: Icon, location: MainRouter.Loc)

  class Backend(t: BackendScope[Props, _]) extends OnUnmount {
    def mounted(): Unit = {
      val obsItems = t.props.todos.foreach(_ ⇒ t.forceUpdate())
      onUnmount(obsItems.kill())
    }
  }

  private def buildTodoMenu(props: Props): ReactNode = {
    val todoCount = props.todos() count (!_.completed)

    Seq(
      <.span("Todo "),
      if (todoCount > 0) <.span(bss.labelOpt(CommonStyle.danger), bss.labelAsBadge, todoCount) else <.span()
    )
  }

  private val menuItems = Seq(
    MenuItem(_ ⇒ "Dashboard", Icon.dashboard, MainRouter.dashboardLoc),
    MenuItem(buildTodoMenu, Icon.check, MainRouter.todoLoc),
    MenuItem(_ ⇒ "React Todo", Icon.checkCircle, MainRouter.reactTodoLoc),
    MenuItem(_ ⇒ "Comments", Icon.pencil, MainRouter.commentsLoc)
  )

  private val MainMenu = ReactComponentB[Props]("MainMenu")
    .stateless
    .backend(new Backend(_))
    .render((P, _, B) ⇒ {
      <.ul(bss.navbar)(
        for (item ← menuItems) yield {
          <.li((P.activeLocation == item.location) ?= (^.className := "active"),
            MainRouter.routerLink(item.location)(item.icon, " ", item.label(P)))
        }
      )
    })
    .componentDidMount(_.backend.mounted())
    .build

  def apply(props: Props) = MainMenu(props)
}
