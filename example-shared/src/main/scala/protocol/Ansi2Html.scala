package protocol

import protocol.Ansi2Html._

import scala.annotation.tailrec
import scala.util.matching.Regex

/**
 * @author steven
 *
 */

object Ansi2Html {
    private val baseStyles = Map(
        "ef0" → "color:#000",
        "ef1" → "color:#A00",
        "ef2" → "color:#0A0",
        "ef3" → "color:#A50",
        "ef4" → "color:#00A",
        "ef5" → "color:#A0A",
        "ef6" → "color:#0AA",
        "ef7" → "color:#AAA",
        "ef8" → "color:#555",
        "ef9" → "color:#F55",
        "ef10" → "color:#5F5",
        "ef11" → "color:#FF5",
        "ef12" → "color:#55F",
        "ef13" → "color:#F5F",
        "ef14" → "color:#5FF",
        "ef15" → "color:#FFF",
        "eb0" → "background-color:#000",
        "eb1" → "background-color:#A00",
        "eb2" → "background-color:#0A0",
        "eb3" → "background-color:#A50",
        "eb4" → "background-color:#00A",
        "eb5" → "background-color:#A0A",
        "eb6" → "background-color:#0AA",
        "eb7" → "background-color:#AAA",
        "eb8" → "background-color:#555",
        "eb9" → "background-color:#F55",
        "eb10" → "background-color:#5F5",
        "eb11" → "background-color:#FF5",
        "eb12" → "background-color:#55F",
        "eb13" → "background-color:#F5F",
        "eb14" → "background-color:#5FF",
        "eb15" → "background-color:#FFF"
    )

    private val colorStyles = {
        val colorPairs = for {
            red   ← 0 to 5
            green ← 0 to 5
            blue  ← 0 to 5
        } yield {
            val c = 16 + (red * 36) + (green * 6) + blue
            val r = if (red > 0) red * 40 + 55 else 0
            val g = if (green > 0) green * 40 + 55 else 0
            val b = if (blue > 0) blue * 40 + 55 else 0
            val rgb = Seq(r, g, b) map toHexString mkString ""
            (s"ef$c" → s"color:#$rgb", s"eb$c" → s"background-color:#$rgb")
        }

        val (fgs, bgs) = colorPairs.unzip

        (fgs ++ bgs).toMap
    }

    private val greyStyles = {
        val greyPairs = (0 to 23) map (grey ⇒ {
            val c = grey + 232
            val l = toHexString(grey * 10 + 8)
            (s"ef$c" → s"color:#$l$l$l", s"eb$c" → s"background-color:#$l$l$l")
        })

        val (fgs, bgs) = greyPairs.unzip

        (fgs ++ bgs).toMap
    }

    private val styles = baseStyles ++ colorStyles ++ greyStyles

    def toHexString(num: Int) = f"$num%02x"

    private object Regexes {
        val remove1 = "^\\x08+".r
        val remove2 = "^\\x1b\\[[012]?K".r
        val removeXterm = "^\\x1b\\[38;5;(\\d+)m".r

        val newlinesRe = "^\\n+".r

        val ansiRe = "^\\x1b\\[((?:\\d{1,3};?)+|)m".r

        val malformedAnsi = "^\\x1b\\[?[\\d;]{0,3}".r

        val nonAnsiRealText = "^([^\\x1b\\x08\\n]+)".r
    }

    private sealed trait Category
    private object Category {
        case object All extends Category
        case object Bold extends Category
        case object Underline extends Category
        case object Blink extends Category
        case object Hide extends Category
        case object Strike extends Category
        case object ForegroundColor extends Category
        case object BackgroundColor extends Category
        case object NoCategory extends Category
    }

    private sealed trait Token
    private object Token {
        case object Text extends Token
        case object Display extends Token
        case object XTerm256 extends Token
    }

    private case class StickyStackElement(token: Token, data: String, category: Category)
}

case class Ansi2Html(fg: String = "#FFF", bg: String = "#000", newLine: Boolean = false, escapeXml: Boolean = true,
                     stream: Boolean = true) {
    private var input = Vector.empty[String]
    private var stack = List.empty[String]
    private var stickyStack = List.empty[StickyStackElement]

    def toHtml(input0: String): String = {
        input = Vector(input0)
        var buf = Vector.empty[String]

        stickyStack foreach (element ⇒ generateOutput(element.token, element.data, (chunk) ⇒ buf :+= chunk))

        forEach(chunk ⇒ buf :+= chunk)
        input = Vector.empty
        buf mkString ""
    }

    private def forEach(callback: (String) ⇒ Unit) = {
        var buf = ""

        input foreach (chunk ⇒ {
            buf += chunk
            tokenize(buf, (token, data) ⇒ {
                generateOutput(token, data, callback)
                if (stream)
                    updateStickyStack(token, data)
            })
        })

        if (stack.nonEmpty) callback(resetStyles())
    }

    private def generateOutput(token: Token, data: String, callback: (String) ⇒ Unit) = {
        token match {
            case Token.Text ⇒ callback(pushText(data))
            case Token.Display ⇒ handleDisplay(data, callback)
            case Token.XTerm256 ⇒ callback(pushStyle(s"ef$data"))
        }
    }

    private def updateStickyStack(token: Token, data: String) = {
        def notCategory(category: Category)(e: StickyStackElement) = {
            ((category == Category.NoCategory) || e.category != category) && (category != Category.All)
        }

        if (token != Token.Text) {
            stickyStack = stickyStack filter notCategory(categoryForCode(data))
            stickyStack = StickyStackElement(token, data, categoryForCode(data)) :: stickyStack
        }
    }

    private def handleDisplay(code: String, callback: (String) ⇒ Unit) = {
        code.toInt match {
            case -1 ⇒ callback("<br/>")
            case 0 if stack.nonEmpty ⇒ callback(resetStyles())
            case 1 ⇒ callback(pushTag("b"))
            case c if c > 2 && c < 5 ⇒ callback(pushTag("u"))
            case c if c > 4 && c < 7 ⇒ callback(pushTag("blink"))
            case 8 ⇒ callback(pushStyle("display:none"))
            case 9 ⇒ callback(pushTag("strike"))
            case 24 ⇒ callback(closeTag("u"))
            case c if c > 29 && c < 38 ⇒ callback(pushStyle(s"ef${c - 30}"))
            case 39 ⇒ callback(pushStyle(s"color:$fg"))
            case c if c > 39 && c < 48 ⇒ callback(pushStyle(s"eb${c - 40}"))
            case 49 ⇒ callback(pushStyle(s"background-color:$bg"))
            case c if c > 89 && c < 98 ⇒ callback(pushStyle(s"ef${8 + (c - 90)}"))
            case c if c > 99 && c < 108 ⇒ callback(pushStyle(s"eb${8 + (c - 100)}"))
            case _ ⇒
        }
    }

    private def categoryForCode(code: String) = {
        code.toInt match {
            case 0 ⇒ Category.All
            case 1 ⇒ Category.Bold
            case c if c > 2 && c < 5 ⇒ Category.Underline
            case c if c > 4 && c < 7 ⇒ Category.Blink
            case 8 ⇒ Category.Hide
            case 9 ⇒ Category.Strike
            case c if (c > 29 && c < 38) || (c == 39) || (c > 89 && c < 98) ⇒ Category.ForegroundColor
            case c if (c > 39 && c < 48) || (c == 49) || (c > 99 && c < 108) ⇒ Category.BackgroundColor
            case _ ⇒ Category.NoCategory
        }
    }

    private def pushTag(tag: String, styleOpt: Option[String] = None) = {
        val style = styleOpt map (s ⇒ {
            if (s.contains(":"))
                s
            else
                styles(s)
        })

        val styStr = style map (s ⇒ s""" style="$s"""")

        stack = tag :: stack

        s"<$tag${styStr.getOrElse("")}>"
    }

    private def pushText(text: String) = {
        if (escapeXml) xml.Utility.escape(text)
        else text
    }

    private def pushStyle(style: String) = pushTag("span", Some(style))

    private def closeTag(style: String) = {
        val last = stack match {
            case x :: xs if x == style ⇒
                stack = stack.tail
                true

            case _ ⇒ false
        }

        if (last) s"</$style>" else ""
    }

    private def resetStyles() = {
        val newStack = stack
        stack = Nil

        newStack.reverse map (tag ⇒ s"</$tag>") mkString ""
    }

    private def tokenize(text0: String, callback: (Token, String) ⇒ Unit) = {
        var ansiMatch = false
        val ansiHandler = 3

        val remove = (m: Regex.Match) ⇒  ""

        val removeXterm256 = (m: Regex.Match) ⇒ {
            callback(Token.XTerm256, m.group(1))
            ""
        }

        val newline = (m: Regex.Match) ⇒ {
            if (newLine)
                callback(Token.Display, "-1")
            else
                callback(Token.Text, m.matched)

            ""
        }

        val ansiMess = (m: Regex.Match) ⇒ {
            ansiMatch = true

            val g1 = {
                if (m.group(1).trim.isEmpty) Array("0")
                else m.group(1).trim.split(";")
            }

            g1 foreach (code ⇒ callback(Token.Display, code))

            ""
        }

        val realText = (m: Regex.Match) ⇒ {
            callback(Token.Text, m.matched)
            ""
        }

        val tokens = Seq(
            Regexes.remove1 → remove,
            Regexes.remove2 → remove,
            Regexes.removeXterm → removeXterm256,

            Regexes.newlinesRe → newline,

            Regexes.ansiRe → ansiMess,

            Regexes.malformedAnsi → remove,

            Regexes.nonAnsiRealText → realText
        )

        var text = text0

        def process(handler: (Regex, (Regex.Match) ⇒ String), i: Int): Unit = {
            if (i <= ansiHandler || !ansiMatch) {
                ansiMatch = false
                //val matches = handler._1.findFirstIn(text).nonEmpty
                text = handler._1.replaceAllIn(text, handler._2)
            }
        }

        @tailrec
        def loop(prevLength: Int): Unit = {
            if (text.length > 0) {
                tokens.zipWithIndex foreach (ti ⇒ process(ti._1, ti._2))
                if (text.length != prevLength)
                    loop(text.length)
            }
        }

        loop(text.length)
    }
}
