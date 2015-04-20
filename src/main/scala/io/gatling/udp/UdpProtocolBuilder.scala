package io.gatling.udp

import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.ChannelHandler

/**
 * @author carlos.raphael.lopes@gmail.com
 */
case object UdpProtocolBuilderAddressStep {
  def address(address: String) = UdpProtocolBuilderPortStep(address)
}

case class UdpProtocolBuilderPortStep(address: String) {
  def port(port: Int) = UdpProtocolBuilderChannelHandlerStep(address, port)
}

case class UdpProtocolBuilderChannelHandlerStep(address: String, port: Int) {
  def channelHandlers(handlers: ChannelHandler*) = 
    UdpProtocolBuilder(address: String, port: Int, handlers.toList: List[ChannelHandler])
}

case class UdpProtocolBuilder(
    address: String, 
    port: Int, 
    handlers: List[ChannelHandler]) {
  
  def build() = new UdpProtocol(
      address = address, 
      port = port,
      handlers = handlers)
}