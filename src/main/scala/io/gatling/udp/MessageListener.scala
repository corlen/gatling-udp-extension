package io.gatling.udp

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler

import com.typesafe.scalalogging.StrictLogging

import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import io.gatling.core.util.TimeHelper.nowMillis

/**
 * Simple channel upstream handler to get response message and notify UdpTrackerActor
 * 
 * @author carlos.raphael.lopes@gmail.com
 */
case class MessageListener(tx: UdpTx, tracker: ActorRef) extends SimpleChannelUpstreamHandler with StrictLogging {
  
  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) = {
    tracker ! MessageReceived(tx.session.userId, nowMillis, new UdpMessage(e.getMessage))
    ctx.sendUpstream(e);
  }
  
  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent): Unit = {
    tracker ! MessageReceived(tx.session.userId, nowMillis, new UdpMessage(e.getCause))
    ctx.sendUpstream(e);
  }
}