package io.gatling.udp.check

import io.gatling.udp.UdpMessage
import scala.concurrent.duration.FiniteDuration
import io.gatling.core.session.Session
import io.gatling.core.validation.Validation
import io.gatling.core.check.CheckResult

/**
 * @author carlos.raphael.lopes@gmail.com
 */
trait UdpCheckSupport {
  def udpCheck(validator: UdpMessage => Validation[CheckResult], timeout: FiniteDuration) = new UdpSimpleCheck(validator, timeout)
}