package io.gatling.udp.action

import akka.actor.ActorDSL.actor
import akka.actor.ActorRef
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.Protocols
import io.gatling.core.session.Expression
import io.gatling.udp.UdpCheck
import io.gatling.udp.UdpMessage
import io.gatling.udp.UdpProtocol

/**
 * @author carlos.raphael.lopes@gmail.com
 */
class UdpConnectActionBuilder(requestName: Expression[String]) extends ActionBuilder {

  /**
   * @param next the Action that will be chained with the Action build by this builder
   * @param protocols the protocols configurations
   * @return the resulting Action actor
   */
  override def build(next: ActorRef, protocols: Protocols): ActorRef = {
    val udpProtocol = protocols.getProtocol[UdpProtocol].getOrElse(throw new UnsupportedOperationException("Udp Protocol wasn't registered"))
    actor(actorName("udpConnect"))(new UdpConnectAction(requestName, udpProtocol, next))
  }
}

class UdpSendActionBuilder(requestName: Expression[String], message: Option[UdpMessage] = None, checkBuilder: Option[UdpCheck] = None) extends ActionBuilder {

  /**
   * @param next the Action that will be chained with the Action build by this builder
   * @param protocols the protocols configurations
   * @return the resulting Action actor
   */
  override def build(next: ActorRef, protocols: Protocols): ActorRef = {
    actor(actorName("udpSend"))(new UdpSendAction(requestName, next, message, checkBuilder))
  }

  def check(checkBuilder: UdpCheck) = new UdpSendActionBuilder(requestName, message, Some(checkBuilder))
}

class UdpDisconnectActionBuilder(requestName: Expression[String]) extends ActionBuilder {
  /**
   * @param next the Action that will be chained with the Action build by this builder
   * @param protocols the protocols configurations
   * @return the resulting Action actor
   */
  override def build(next: ActorRef, protocols: Protocols): ActorRef = {
    actor(actorName("udpDisconnect"))(new UdpDisconnectAction(requestName, next))
  }
}