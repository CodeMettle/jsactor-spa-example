package example.logging

import com.github.pimterry.loglevel.LogLevel

/**
 * @author steven
 *
 */
trait WebLogging {
  protected val logger = LogLevel.log
}
