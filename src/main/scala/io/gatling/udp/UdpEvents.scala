package io.gatling.udp

import akka.actor.ActorRef
import io.gatling.core.session.Session

/**
 * define trait for udp events 
 * 
 * @author carlos.raphael.lopes@gmail.com
 */
sealed trait UdpEvents

case class Connect(tx: UdpTx) extends UdpEvents

case class Disconnect(title: String, next: ActorRef, session: Session) extends UdpEvents

case class CheckTimeout(check: UdpCheck, sentTime: Long) extends UdpEvents

case class MessageSend(
    requestName: String,
    check: UdpCheck,
    message: Option[UdpMessage],
		session: Session,
    next: ActorRef) extends UdpEvents

case class MessageReceived(responseId: String, received: Long, message: UdpMessage) extends UdpEvents