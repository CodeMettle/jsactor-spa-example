package protocol.xml

/**
 * @author scala
 *
 */
object Utility {
  // helper for the extremely oft-repeated sequence of creating a
  // StringBuilder, passing it around, and then grabbing its String.
  private[xml] def sbToString(f: (StringBuilder) => Unit): String = {
    val sb = new StringBuilder
    f(sb)
    sb.toString
  }

  /**
   * Escapes the characters &lt; &gt; &amp; and &quot; from string.
   */
  final def escape(text: String): String = sbToString(escape(text, _))

  object Escapes {
    /**
     * For reasons unclear escape and unescape are a long ways from
     * being logical inverses.
     */
    val pairs = Map(
      "lt" -> '<',
      "gt" -> '>',
      "amp" -> '&',
      "quot" -> '"'
      // enigmatic comment explaining why this isn't escaped --
      // is valid xhtml but not html, and IE doesn't know it, says jweb
      // "apos"  -> '\''
    )
    val escMap = pairs map { case (s, c) => c -> ("&%s;" format s) }
    val unescMap = pairs ++ Map("apos" -> '\'')
  }
  import Escapes.unescMap

  /**
   * Appends escaped string to `s`.
   */
  final def escape(text: String, s: StringBuilder): StringBuilder = {
    // Implemented per XML spec:
    // http://www.w3.org/International/questions/qa-controls
    // imperative code 3x-4x faster than current implementation
    // dpp (David Pollak) 2010/02/03
    val len = text.length
    var pos = 0
    while (pos < len) {
      text.charAt(pos) match {
        case '<'  => s.append("&lt;")
        case '>'  => s.append("&gt;")
        case '&'  => s.append("&amp;")
        case '"'  => s.append("&quot;")
        case '\n' => s.append('\n')
        case '\r' => s.append('\r')
        case '\t' => s.append('\t')
        case c    => if (c >= ' ') s.append(c)
      }

      pos += 1
    }
    s
  }

  /**
   * Appends unescaped string to `s`, `amp` becomes `&amp;`,
   * `lt` becomes `&lt;` etc..
   *
   * @return    `'''null'''` if `ref` was not a predefined entity.
   */
  final def unescape(ref: String, s: StringBuilder): StringBuilder =
    ((unescMap get ref) map (s append _)).orNull

}
