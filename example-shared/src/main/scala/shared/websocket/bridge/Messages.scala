package shared.websocket.bridge

import upickle.default._

import com.codemettle.jsactorexample.web.shared._

import jsactor.bridge.protocol.UPickleBridgeProtocol
import jsactor.bridge.protocol.UPickleBridgeProtocol.MessageRegistry
import scala.reflect.ClassTag

/**
 * @author steven
 *
 */
object Messages extends UPickleBridgeProtocol {

  override def registerMessages(registry: MessageRegistry): Unit = {
    def add[A : Reader : Writer : ClassTag] = {
      registry.add[A]
    }

    def addObj[A <: Singleton : Reader : Writer : ClassTag](obj: A) = {
      registry.addObj(obj)
    }

    addObj(ServerTime.Subscribe)
    add[ServerTime.ServerTime]
  }

}
