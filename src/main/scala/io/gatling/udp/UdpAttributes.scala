package io.gatling.udp

import org.jboss.netty.channel.Channel

import io.gatling.core.session.Expression

case class UdpAttributes(requestName: Expression[String], message: UdpMessage, checks: List[UdpCheck] = Nil, var channel: Channel = null) {
}