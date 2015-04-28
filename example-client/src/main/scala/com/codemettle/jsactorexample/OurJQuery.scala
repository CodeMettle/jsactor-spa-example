package com.codemettle.jsactorexample

import org.querki.jquery.JQuery

import scala.scalajs.js

/**
 * @author steven
 *
 */
trait OurJQuery extends JQuery {
  /* Bootstrap */
  def modal(action: String): OurJQuery = js.native
  def modal(options: js.Any): OurJQuery = js.native
  def collapse(option: String): OurJQuery = js.native

  def button(textState: String): OurJQuery = js.native
}
