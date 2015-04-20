package io.gatling.udp

import org.jboss.netty.channel.ChannelHandler
import io.gatling.core.config.Protocol

/**
 * @author carlos.raphael.lopes@gmail.com
 */
case class UdpProtocol(
    address: String, 
    port: Int,
    var handlers: List[ChannelHandler]) extends Protocol {
}