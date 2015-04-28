package com.codemettle.jsactorexample.web.shared

/**
 * @author steven
 *
 */
object ServerTime {
  case object Subscribe
  case class ServerTime(date: String)
}
