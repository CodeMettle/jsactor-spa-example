package com.codemettle.jsactorexample
package web.components

import japgolly.scalacss.ScalaCssReact._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.querki.jquery._

import scala.language.implicitConversions
import scala.scalajs.js

/**
 * @author steven
 *
 */
object Bootstrap {

  @inline private def bss = GlobalStyles.bootstrapStyles

  object CommonStyle extends Enumeration {
    val default, primary, success, info, warning, danger = Value
  }

  object Panel {

    case class Props(heading: String, style: CommonStyle.Value = CommonStyle.default, addl: Seq[TagMod] = Seq.empty)

    val component = ReactComponentB[Props]("Panel").render((P, C) ⇒ {
      <.div(bss.panelOpt(P.style), P.addl)(
        <.div(bss.panelHeading)(P.heading),
        <.div(bss.panelBody)(C)
      )
    }).build

    def apply(props: Props)(children: ReactNode*) = component(props, children)
  }

  object Modal {

    case class Props(header: (Backend) ⇒ ReactNode, footer: (Backend) ⇒ ReactNode, closed: () ⇒ Unit,
                     backdrop: Boolean = true, keyboard: Boolean = true)

    class Backend(t: BackendScope[Props, Unit]) {
      def hide(): Unit = {
        $(t.getDOMNode()).modal("hide")
      }

      def hidden(e: JQEvt): js.Any = {
        t.props.closed()
      }
    }

    val component = ReactComponentB[Props]("Modal")
      .stateless
      .backend(new Backend(_))
      .render((P, C, _, B) ⇒ {
        val modalStyle = bss.modal
        <.div(modalStyle.modal, modalStyle.fade, ^.role := "dialog", ^.aria.hidden := true,
          <.div(modalStyle.dialog,
            <.div(modalStyle.content,
              <.div(modalStyle.header, P.header(B)),
              <.div(modalStyle.body, C),
              <.div(modalStyle.footer, P.footer(B))
            )
          )
        )
      })
      .componentDidMount(scope ⇒ {
        val P = scope.props
        $(scope.getDOMNode()).modal(js.Dynamic.literal(backdrop = P.backdrop, keyboard = P.keyboard, show = true))
        $(scope.getDOMNode()).on("hidden.bs.modal", scope.backend.hidden _)
      })
      .build

    def apply(props: Props, children: ReactNode*) = component(props, children)
    def apply() = component
  }

}
