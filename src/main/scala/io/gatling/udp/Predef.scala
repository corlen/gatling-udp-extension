package io.gatling.udp

import io.gatling.core.session.Expression
import io.gatling.udp.check.UdpCheckDsl

/**
 * Predef object to be used to simplify the DSL 
 * 
 * @author carlos.raphael.lopes@gmail.com
 */
object Predef extends UdpCheckDsl {
  
  val udp = UdpProtocolBuilderAddressStep

  def udp(requestName: Expression[String]) = new Udp(requestName)
  
  implicit def udpBuilderToProtocol(builder: UdpProtocolBuilder): UdpProtocol = builder.build()
}