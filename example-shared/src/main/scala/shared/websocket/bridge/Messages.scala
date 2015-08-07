package shared.websocket.bridge

import com.codemettle.jsactorexample.web.shared._
import jsactor.bridge.protocol.BridgeProtocol
import jsactor.bridge.protocol.BridgeProtocol.MessageRegistry
import upickle.default._

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

    add[Api.DeleteTodo]
    add[Api.TodoDeleted]
    add[Api.UpdateTodo]
    addObj(Api.SubscribeToTodos)
    add[Api.GetMotd]
    add[Api.GotMotd]
    add[Api.TodosChanged]

    addObj(CommentsApi.SubscribeToComments)
    add[CommentsApi.UpdatedComments]
    add[CommentsApi.AddComment]
  }

}
