package example

import com.codemettle.jsactorexample.web.settings
import com.codemettle.jsactorexample.web.shared.ServerTime
import jsactor.bridge.client.util.RemoteActorListener
import jsactor.{JsActorRef, JsProps, JsActorSystem}
import jsactor.bridge.client.SocketManager
import org.scalajs.dom
import shared.websocket.bridge.Messages

import scala.scalajs.js.JSApp

object ScalaJSExample extends JSApp {
  val actorSystem = JsActorSystem("test")

  def main(): Unit = {
    val container = dom.document.getElementById("container")

    implicit val protocol = Messages

    val _wsManager = actorSystem.actorOf(SocketManager.props(SocketManager.Config(settings.wsUrl)), "socketManager")

    actorSystem.actorOf(JsProps(new RemoteActorListener {
      override def onConnect(serverActor: JsActorRef): Unit = serverActor ! ServerTime.Subscribe

      override def wsManager: JsActorRef = _wsManager

      override def actorPath: String = "/user/serverTime"

      override def whenConnected(serverActor: JsActorRef): Receive = {
        case ServerTime.ServerTime(dateStr) ⇒ container.innerHTML = s"Server Time: $dateStr"
      }
    }), "serverTime")
  }
}
