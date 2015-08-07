package com.codemettle.jsactorexample
package web.services

import com.github.pimterry.loglevel.LogLevel
import japgolly.scalajs.react.React
import jsactor.JsActorSystem
import jsactor.bridge.client.{WebSocketManager, SocketManager}
import jsactor.logging.impl.JsLoglevelActorLoggerFactory
import org.querki.jquery._
import org.scalajs.dom
import rx._
import shared.websocket.bridge.Messages

import com.codemettle.jsactorexample.web.modules.MainRouter
import com.codemettle.jsactorexample.web.settings

/**
 * @author steven
 *
 */
object Application {
  val actorSystem = JsActorSystem("Example", JsLoglevelActorLoggerFactory)

  val wsManager = {
    implicit def protocol = Messages
    implicit def system = actorSystem

    new WebSocketManager(SocketManager.Config(settings.appSettings.webSocketUrl))
  }

  val todoStore = actorSystem.actorOf(TodoStore.props(wsManager), "todoStore")

  private val _winHeight = Var[Double]($(dom.window).height())
  def windowHeight: Rx[Double] = _winHeight

  def start(): Unit = {
    LogLevel.log.setLevel(settings.appSettings.logLevel)

    $(dom.window).resize((e: JQEvt) ⇒ _winHeight() = $(e.target).height())

    React.render(MainRouter.routerComponent(), dom.document.body)
  }
}
