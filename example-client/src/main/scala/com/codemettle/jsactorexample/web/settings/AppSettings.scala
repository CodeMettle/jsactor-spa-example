package com.codemettle.jsactorexample.web.settings

import scala.scalajs.js

/**
 * @author steven
 *
 */
trait AppSettings extends js.Object {
  val baseUrl: String = js.native
  val logLevel: String = js.native
  val username: String = js.native
  val webSocketUrl: String = js.native
}
