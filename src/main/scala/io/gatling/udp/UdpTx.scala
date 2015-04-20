package io.gatling.udp

import io.gatling.core.session.Session
import akka.actor.ActorRef

/**
 * @author carlos.raphael.lopes@gmail.com
 */
case class UdpTx(
    session: Session, 
    next:ActorRef,
    var requestName: String, 
    protocol: UdpProtocol, 
    start: Long,
    var message: UdpMessage = null,
    updates: List[Session => Session] = Nil) {
  def applyUpdates(session: Session) = {
    val newSession = session.update(updates)
    copy(session = newSession, updates = Nil)
  }
}