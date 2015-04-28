package com.codemettle

import org.querki.jquery.JQuery

/**
 * @author steven
 *
 */
package object jsactorexample {
  import scala.language.implicitConversions
  implicit def jq2ojq(jq: JQuery): OurJQuery = jq.asInstanceOf[OurJQuery]
}
