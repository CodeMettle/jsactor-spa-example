package com.codemettle.jsactorexample.web.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.TagMod
import japgolly.scalajs.react.vdom.prefix_<^._

import com.codemettle.jsactorexample.web.widgets.Button.Size.Size
import com.codemettle.jsactorexample.web.widgets.Button.Style.Style

import scala.scalajs.js
import scala.scalajs.js.UndefOr

/**
 * @author steven
 *
 */
object Button {
  object Style extends Enumeration {
    type Style = Value
    val default, primary, success, info, warning, danger, link = Value
  }

  object Size extends Enumeration {
    type Size = Value
    val lg, sm, xs = Value
  }

  case class Props(onClick: (ReactEventI) ⇒ Unit,
                   style: Style = Style.default,
                   size: UndefOr[Size] = js.undefined,
                   disabled: Boolean = false,
                   onDblclick: (ReactEventI) ⇒ Unit = _ ⇒ {},
                   addl: Seq[TagMod] = Seq.empty
                    )

  val component = ReactComponentB[Props]("Button")
    .stateless
    .noBackend
    .render((P, C, _, B) ⇒ {
      <.button(^.className := s"btn btn-${P.style}", ^.className := (P.size map (s ⇒ s"btn-$s")),
        ^.onClick ==> P.onClick, ^.onDblClick ==> P.onDblclick, P.disabled ?= (^.disabled := "disabled"), P.addl)(C)
    })
    .build

  def apply(onClick: (ReactEventI) ⇒ Unit,
            style: Style = Style.default,
            size: UndefOr[Size] = js.undefined,
            disabled: Boolean = false,
            onDblclick: (ReactEventI) ⇒ Unit = _ ⇒ {},
            addl: Seq[TagMod] = Seq.empty
             )(children: ReactNode*) = {
    component(Props(onClick, style, size, disabled, onDblclick, addl), children)
  }

  def identify(key: UndefOr[js.Any] = js.undefined, ref: UndefOr[String] = js.undefined)
              (onClick: (ReactEventI) ⇒ Unit,
               style: Style = Style.default,
               size: UndefOr[Size] = js.undefined,
               disabled: Boolean = false,
               onDblclick: (ReactEventI) ⇒ Unit = _ ⇒ {},
               addl: Seq[TagMod] = Seq.empty
                )(children: ReactNode*) = {
    component.set(key = key, ref = ref)(Props(onClick, style, size, disabled, onDblclick, addl), children)
  }
}
