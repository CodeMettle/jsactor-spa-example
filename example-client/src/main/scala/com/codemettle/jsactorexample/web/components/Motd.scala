package com.codemettle.jsactorexample.web.components

import com.github.pimterry.loglevel.LogLevel
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import jsactor.JsTimeout
import jsactor.pattern.ask
import org.querki.jquery._
import org.scalajs.dom.html.Button

import com.codemettle.jsactorexample.web.components.Bootstrap.Panel
import com.codemettle.jsactorexample.web.services.Application
import com.codemettle.jsactorexample.web.settings
import com.codemettle.jsactorexample.web.shared.Api
import com.codemettle.jsactorexample.web.widgets.Button

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

/**
 * @author steven
 *
 */
object Motd {
  case class State(message: String, loading: Boolean = false)

  private val loadBtn = Ref.to(Button.component, "refreshMotdBtn")
  private val loadBtn2 = Ref[Button]("refreshMotdBtn")

  class Backend(t: BackendScope[Unit, State]) {
    def doRefresh(): Unit = {
      t.modState(_.copy(loading = true))

      implicit val timeout = JsTimeout(5.seconds)
      (Application.todoStore ? Api.GetMotd(settings.appSettings.username)) onComplete {
        case Success(motd: Api.GotMotd) ⇒ t.modState(_.copy(message = motd.motd, loading = false))

        case Success(_) ⇒ t.modState(_.copy(loading = false))

        case Failure(err) ⇒ t.modState(_.copy(message = s"Error: ${err.getMessage}", loading = false))
      }
    }

    def refresh(e: ReactEventI): Unit = {
      doRefresh()
    }

    def click(): Unit = {
      // don't actually click on things or trigger behavior from UI if at all possible - just run code that changes
      // state and have your components render themselves properly. was just testing
      LogLevel.log.info(s"${t.refs("refreshMotdBtn")}")
      loadBtn(t) map (_.getDOMNode()) foreach (n ⇒ LogLevel.log.info(s"$n"))
      loadBtn2(t) map (_.getDOMNode()) foreach (n ⇒ LogLevel.log.info(s"$n"))
      loadBtn(t) map (b ⇒ $(b.getDOMNode())) foreach (_.click())
    }
  }

  val loadingText = "data-loading-text".reactAttr

  val component = ReactComponentB[Unit]("Motd")
    .initialState(State("Loading..."))
    .backend(new Backend(_))
    .render((_, S, B) ⇒ {
      Panel(Panel.Props("Message of the day - artificial delay on server"))(
        <.div(
          S.message,
          Button.identify(ref = "refreshMotdBtn")(B.refresh, Button.Style.danger, disabled = S.loading,
            addl = Seq(^.className := "pull-right"))(
              if (S.loading)
                Seq("Loading...": ReactNode)
              else
                Seq[ReactNode](Icon.refresh, " Update")
            )
        )
      )
    })
    .componentDidMount(_.backend.click() /*<-- shouldn't do this*/ /*_.backend.doRefresh()*/)
    .buildU

  def apply() = component()
}
