package shared.websocket.bridge

import jsactor.bridge.protocol.BridgeProtocol
import jsactor.bridge.protocol.BridgeProtocol.MessageRegistry
import upickle._

import com.codemettle.jsactorexample.web.shared._

import scala.reflect.ClassTag

/**
 * @author steven
 *
 */
object Messages extends BridgeProtocol {

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
