package io.gatling.udp.action

import io.gatling.core.session.Session
import io.gatling.udp.UdpProtocol
import io.gatling.udp.UdpMessage
import io.gatling.core.action.{ Failable, Interruptable }
import io.gatling.core.session.{ Expression, Session }
import io.gatling.core.validation.Validation
import akka.actor.ActorRef
import akka.actor.ActorDSL.actor
import io.gatling.udp.MessageListener
import io.gatling.udp.UdpMessage
import io.gatling.core.util.TimeHelper.nowMillis
import io.gatling.udp.UdpTx
import io.gatling.udp.Disconnect

/**
 * @author carlos.raphael.lopes@gmail.com
 */
class UdpDisconnectAction(val requestName: Expression[String], val next: ActorRef) extends Interruptable with Failable {

  override def executeOrFail(session: Session): Validation[_] = {
    for {
      resolvedRequestName <- requestName(session)
      tracker <- session("udpTracker").validate[ActorRef].mapError(m => s"Couldn't fetch open socket: $m")
    } yield tracker ! Disconnect(resolvedRequestName, next, session)
  }
}