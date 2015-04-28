package controllers

import jsactor.bridge.server.ServerBridgeActor
import play.api.libs.concurrent.Akka
import play.api.mvc._
import shared.websocket.bridge.Messages

import com.codemettle.jsactorexample.web.services.{CommentService, SPAService}

object Application extends Controller {
  import play.api.Play.current

  val todoService = Akka.system.actorOf(SPAService.props, "SPAService")

  val commentService = Akka.system.actorOf(CommentService.props, "CommentService")

  def index(path: String) = Action { implicit request ⇒
    Ok(views.html.index())
  }

  def ws = WebSocket.acceptWithActor[String, String] { req ⇒ websocket ⇒
    implicit val protocol = Messages
    ServerBridgeActor.props(websocket)
  }
}
