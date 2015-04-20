package io.gatling.udp.action

import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import io.gatling.core.action.Failable
import io.gatling.core.action.Interruptable
import io.gatling.core.session.Expression
import io.gatling.core.session.Session
import io.gatling.core.validation.Validation
import io.gatling.udp.MessageSend
import io.gatling.udp.UdpCheck
import io.gatling.udp.UdpMessage

/**
 * @author carlos.raphael.lopes@gmail.com
 */
case class UdpSendAction(val requestName: Expression[String], val next: ActorRef, message: Option[UdpMessage], check: Option[UdpCheck]) extends Interruptable with Failable {

  override def executeOrFail(session: Session): Validation[Unit] = {
    for {
      resolvedRequestName <- requestName(session)
      tracker <- session("udpTracker").validate[ActorRef].mapError(m => s"Couldn't fetch open socket: $m")
    } yield tracker ! MessageSend(resolvedRequestName, check.getOrElse(null), message, session, next)
  }
}