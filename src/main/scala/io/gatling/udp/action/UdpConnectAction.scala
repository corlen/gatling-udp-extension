package io.gatling.udp.action

import akka.actor.ActorDSL.actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import io.gatling.core.action.Failable
import io.gatling.core.action.Interruptable
import io.gatling.core.session.Expression
import io.gatling.core.session.Session
import io.gatling.core.util.TimeHelper.nowMillis
import io.gatling.core.validation.Validation
import io.gatling.udp.Connect
import io.gatling.udp.UdpProtocol
import io.gatling.udp.UdpTrackerActor
import io.gatling.udp.UdpTx

/**
 * @author carlos.raphael.lopes@gmail.com
 */
class UdpConnectAction(requestName: Expression[String], protocol: UdpProtocol, val next: ActorRef) extends Interruptable with Failable {

  override def executeOrFail(session: Session): Validation[_] = {
      def connect(tx: UdpTx): Unit = {
        val tracker = actor(context, actorName("udpRequestTracker"))(new UdpTrackerActor)
        tracker ! Connect(tx)
      }

    for {
      requestName <- requestName(session)
    } yield connect(UdpTx(session, next, requestName = requestName, protocol = protocol, start = nowMillis))
  }
}