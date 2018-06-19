package example

import org.scalajs.dom
import shared.websocket.bridge.Messages

import com.codemettle.jsactorexample.web.settings
import com.codemettle.jsactorexample.web.shared.ServerTime

import jsactor.bridge.client.util.RemoteActorListener
import jsactor.bridge.client.{UPickleSocketManager, WebSocketManager}
import jsactor.bridge.protocol.UPickleBridgeProtocol
import jsactor.{JsActorRef, JsActorSystem, JsProps}

object ScalaJSExample {
  val actorSystem = JsActorSystem("test")

  def main(args: Array[String]): Unit = {
    val container = dom.document.getElementById("container")

    implicit val protocol: UPickleBridgeProtocol = Messages

    val _wsManager = actorSystem.actorOf(UPickleSocketManager.props(UPickleSocketManager.Config(settings.wsUrl)), "socketManager")

    actorSystem.actorOf(JsProps(new RemoteActorListener {
      override def onConnect(serverActor: JsActorRef): Unit = serverActor ! ServerTime.Subscribe

      override val webSocketManager: WebSocketManager = new WebSocketManager {
        override def socketManager: JsActorRef = _wsManager
      }

      override def actorPath: String = "/user/serverTime"

      override def whenConnected(serverActor: JsActorRef): Receive = {
        case ServerTime.ServerTime(dateStr) ⇒ container.innerHTML = s"Server Time: $dateStr"
      }
    }), "serverTime")
  }
}
