package io.gatling.udp.check

import scala.collection.mutable
import io.gatling.core.check.CheckResult
import io.gatling.core.session.Session
import io.gatling.core.validation.Failure
import io.gatling.core.validation.Validation
import io.gatling.udp.UdpMessage
import io.gatling.udp.UdpCheck
import io.gatling.udp.UdpMessage
import scala.concurrent.duration.FiniteDuration
import io.gatling.core.util.TimeHelper._
import io.gatling.core.check.Check

/**
 * @author carlos.raphael.lopes@gmail.com
 */
case class UdpSimpleCheck(validator: UdpMessage => Validation[CheckResult], timeout: FiniteDuration, timestamp: Long = nowMillis) extends Check[UdpMessage] {
  override def check(response: UdpMessage, session: Session)(implicit cache: mutable.Map[Any, Any]): Validation[CheckResult] = {
    validator(response)
  }
}