package controllers

import java.util.Date

import akka.actor.{Terminated, Actor, ActorRef, Props}
import com.codemettle.jsactorexample.web.shared.ServerTime
import jsactor.bridge.server.ServerBridgeActor
import play.api.libs.concurrent.Akka
import play.api.mvc._
import shared.websocket.bridge.Messages

import scala.concurrent.duration._

object Application extends Controller {
  import play.api.Play.current

  val serverTime = Akka.system.actorOf(Props(new Actor {
    import context.dispatcher

    private var subscribers = Set.empty[ActorRef]

    val timer = context.system.scheduler.schedule(1.second, 1.second, self, 'tick)

    override def postStop(): Unit = {
      super.postStop()

      timer.cancel()
    }

    def receive = {
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

  def ws = WebSocket.acceptWithActor[String, String] { req ⇒ websocket ⇒
    implicit val protocol = Messages
    ServerBridgeActor.props(websocket)
  }
}
