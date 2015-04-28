package com.codemettle.jsactorexample.web.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import jsactor.bridge.client.util.RemoteActorListener
import jsactor.logging.JsActorLogging
import jsactor.{JsPoisonPill, JsActorRef, JsProps}
import org.scalajs.dom.html.Input

import com.codemettle.jsactorexample.web.services.Application
import com.codemettle.jsactorexample.web.shared.CommentsApi
import com.codemettle.jsactorexample.web.shared.CommentsApi.Comment
import com.codemettle.jsactorexample.web.widgets.Button

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

/**
 * @author steven
 *
 */
object Showdown extends js.GlobalScope {
  @JSName("Showdown.converter")
  class converter extends js.Object {
    def makeHtml(markdown: String): String = js.native
  }
}

object CommentBox {
  val data = Seq(
    CommentsApi.Comment("Pete Hunt", "This is one comment"),
    CommentsApi.Comment("Jordan Walke", "This is *another* `comment`")
  )

  case class State(data: Seq[CommentsApi.Comment])

  private class ClientActor(t: BackendScope[Unit, State]) extends RemoteActorListener with JsActorLogging {

    override def preStart(): Unit = {
      super.preStart()

      log.info("Comments actor started")
    }

    override def postStop(): Unit = {
      super.postStop()

      log.info("Comments actor stopped")
    }

    override def wsManager: JsActorRef = Application.wsManager

    override def actorPath: String = "/user/CommentService"

    override def onConnect(serverActor: JsActorRef): Unit = serverActor ! CommentsApi.SubscribeToComments

    override def whenConnected(serverActor: JsActorRef): Receive = {
      case CommentsApi.UpdatedComments(comments) ⇒
        log.trace(s"Received comments: $comments")
        t.modState(_.copy(data = comments))

      case ac: CommentsApi.AddComment ⇒ serverActor ! ac
    }
  }

  class Backend(t: BackendScope[Unit, State]) {
    val actor = Application.actorSystem.actorOf(JsProps(new ClientActor(t)))

    def handleCommentSubmit(comment: CommentsApi.Comment) = {
      t.modState(s ⇒ s.copy(data = s.data :+ comment))
      actor ! CommentsApi.AddComment(comment)
    }

    def onUnload() = {
      actor ! JsPoisonPill
    }
  }

  val component = ReactComponentB[Unit]("CommentBox")
    .initialState(State(Seq.empty))
    .backend(new Backend(_))
    .render((_, S, B) ⇒ {
      <.div(^.className := "commentBox")(
        <.h1("Comments"),
        CommentList(S.data),
        CommentForm(B.handleCommentSubmit)
      )
    })
    .componentWillUnmount(_.backend.onUnload())
    .buildU

  def apply() = component()
}

object CommentList {
  case class Props(data: Seq[Comment])

  val component = ReactComponentB[Props]("CommentList")
    .stateless
    .noBackend
    .render((P, _, _) ⇒ {
      val commentNodes = P.data map (comment ⇒ {
        Comment(author = comment.author)(comment.text)
      })

      <.div(^.className := "commentList")(commentNodes)
    })
    .build

  def apply(comments: Seq[Comment]) = component(Props(comments))
}

object CommentForm {
  case class Props(onCommentSubmit: (CommentsApi.Comment) ⇒ Unit)

  class Backend(t: BackendScope[Props, Unit]) {
    def handleSubmit(e: ReactEventI): Unit = {
      //e.preventDefault()
      val author = t.refs[Input]("author") map (_.getDOMNode())
      val text = t.refs[Input]("text") map (_.getDOMNode())

      if (text.nonEmpty && author.nonEmpty) {
        t.props.onCommentSubmit(CommentsApi.Comment(author.get.value, text.get.value))

        author.get.value = ""
        text.get.value = ""
      }
    }
  }

  val component = ReactComponentB[Props]("CommentForm")
    .stateless
    .backend(new Backend(_))
    .render((_, _, B) ⇒ {
      <.div(//form(^.className := "commentForm", ^.onSubmit ==> B.handleSubmit)(
        <.div(<.input(^.`type` := "text", ^.placeholder := "Your name", ^.ref := "author")),
        <.div(<.textarea(^.placeholder := "Say something...", ^.ref := "text")),
        //<.div(<.input(^.`type` := "submit", ^.value := "Post"))
        Button(B.handleSubmit, Button.Style.success)("Post")
      )
    })
    .build

  def apply(onCommentSubmit: (CommentsApi.Comment) ⇒ Unit) = component(Props(onCommentSubmit))
}

object Comment {
  val converter = new Showdown.converter

  case class Props(author: String)

  val component = ReactComponentB[Props]("Comment")
    .stateless
    .noBackend
    .render((P, C, _, _) ⇒ {
      <.div(^.className := "comment")(
        <.h2(^.className := "commentAuthor")(P.author),
        <.div(^.dangerouslySetInnerHtml(converter.makeHtml(C.toString)))
      )
    })
    .build

  def apply(author: String)(children: ReactNode*) = component(Props(author), children)
}
