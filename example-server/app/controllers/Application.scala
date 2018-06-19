package controllers

import java.util.Date

import javax.inject.{Inject, Singleton}
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import shared.websocket.bridge.Messages

import com.codemettle.jsactorexample.web.shared.ServerTime

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.stream.Materializer
import jsactor.bridge.protocol.UPickleBridgeProtocol
import jsactor.bridge.server.UPickleServerBridgeActor
import scala.concurrent.duration._

@Singleton
class Application @Inject()(cc: ControllerComponents)(implicit actSys: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  actSys.actorOf(Props(new Actor {
    import context.dispatcher

    private var subscribers = Set.empty[ActorRef]

    private val timer = context.system.scheduler.schedule(1.second, 1.second, self, 'tick)

    override def postStop(): Unit = {
      super.postStop()

      timer.cancel()
    }

    override def receive: Receive = {
      case ServerTime.Subscribe ⇒
        context watch sender()
        subscribers += sender()

      case Terminated(act) ⇒ subscribers -= act

      case 'tick ⇒
        val now = new Date
        val msg = ServerTime.ServerTime(now.toString)
        subscribers foreach (_ ! msg)
    }
  }), "serverTime")

  def index(path: String) = Action { implicit request ⇒
    Ok(views.html.index())
  }

  def ws = WebSocket.accept[String, String] { implicit req ⇒
    implicit val msgs: UPickleBridgeProtocol = Messages
    ActorFlow.actorRef(UPickleServerBridgeActor.props)
  }
}
