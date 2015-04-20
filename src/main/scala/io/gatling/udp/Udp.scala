package io.gatling.udp

import io.gatling.core.session.Expression
import io.gatling.udp.action.UdpConnectActionBuilder
import io.gatling.udp.action.UdpSendActionBuilder
import io.gatling.udp.action.UdpSendActionBuilder
import io.gatling.udp.action.UdpDisconnectActionBuilder

/**
 * @author carlos.raphael.lopes@gmail.com
 */
class Udp(requestName: Expression[String]) {

  def connect() = new UdpConnectActionBuilder(requestName)

  def send(message: Any) = new UdpSendActionBuilder(requestName, Some(UdpMessage(message)))
  
  /**
   * You have to configure a feeder which maps "udpMessage"
   */
  def send() = new UdpSendActionBuilder(requestName)

  def disconnect() = new UdpDisconnectActionBuilder(requestName)
}