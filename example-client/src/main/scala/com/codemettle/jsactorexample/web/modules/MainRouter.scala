package com.codemettle.jsactorexample
package web
package modules

import japgolly.scalajs.react.ReactElement
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.querki.jquery.$
import org.scalajs.dom

import com.codemettle.jsactorexample.web.components.Icon
import com.codemettle.jsactorexample.web.services.TodoStore

/**
 * @author steven
 *
 */
object AppLinks {

}

object MainRouter extends RoutingRules {
  val baseUrl = BaseUrl(settings.appSettings.baseUrl)

  val dashboardLoc = register(rootLocation(Dashboard.component))
  val todoLoc = register(location("/todo", Todo()))
  val reactTodoLoc = register(location("/reactTodo", ReactTodoApp()))
  val commentsLoc = register(location("/comments", _ ⇒ CommentBox()))

  def dashboardLink = router.link(dashboardLoc)
  def todoLink = router.link(todoLoc)

  def routerLink(loc: Loc) = router.link(loc)

  private def setWindowTitle(loc: Loc) = {
    val title = loc match {
      case `dashboardLoc` ⇒ "Dashboard"
      case `todoLoc` ⇒ "Todo"
      case `reactTodoLoc` ⇒ "React Todos"
      case `commentsLoc` ⇒ "Comments"
    }

    dom.document.title = s"SPA | $title"
  }

  onRouteChange(loc ⇒ {
    dom.window.scrollTo(0, 0)
    val mainNav = $("#main-nav")
    if (mainNav.hasClass("in"))
      mainNav.collapse("hide")

    setWindowTitle(loc)
  })

  val router = routingEngine(baseUrl)
  val routerComponent = {
    Router.componentUnbuilt(router)
      .componentDidMount(scope ⇒ setWindowTitle(scope.state))
      .buildU
  }

  override protected val notFound = redirect(dashboardLoc, Redirect.Replace)

  private val toggle = "data-toggle".reactAttr
  private val target = "data-target".reactAttr

  override protected def interceptRender(ic: InterceptionR): ReactElement = {
    <.div(
      <.nav(^.className := "navbar navbar-default navbar-fixed-top")(
        <.div(^.className := "container")(
          <.div(^.className := "navbar-header")(
            <.button(^.className := "navbar-toggle collapsed", toggle := "collapse", target := "#main-nav")(
              <.span(^.className := "sr-only")("Toggle navigation"),
              <.span(^.className := "icon-bar"),
              <.span(^.className := "icon-bar"),
              <.span(^.className := "icon-bar")
            ),
            <.a(^.className := "navbar-brand", ^.href := "#")("SPA Tutorial")
          ),
          <.div(^.className := "collapse navbar-collapse", ^.id := "main-nav")(
            MainMenu(MainMenu.Props(ic.loc, TodoStore.todos)),
            <.ul(^.className := "nav navbar-nav navbar-right")(
              <.li(^.className := "dropdown")(
                <.a(^.href := "#", ^.className := "dropdown-toggle", toggle := "dropdown", ^.role := "button",
                  ^.aria.expanded := "false")(s"Signed in as ", settings.appSettings.username, <.span(^.className := "caret")),
                <.ul(^.className := "dropdown-menu", ^.role := "menu")(
                  <.li(<.a(^.href := "#")(Icon.cogs, " Preferences")),
                  <.li(^.className := "divider"),
                  <.li(<.a(^.href := "#")(Icon.signOut, " Logout"))
                )
              )
            )
          )
        )
      ),
      <.div(^.className := "container")(ic.element)
    )
  }
}
