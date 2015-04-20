package io.gatling.udp.check

import io.gatling.core.session._
import scala.concurrent.duration.FiniteDuration
import io.gatling.udp.UdpMessage
import io.gatling.core.validation.Validation
import io.gatling.core.check.CheckResult

/**
 * @author carlos.raphael.lopes@gmail.com
 */
trait UdpCheckDsl {

  val udpCheck: Step2 = new Step2

  class Step2() {

    def within(timeout: FiniteDuration) = new Step3(timeout)
  }

  class Step3(timeout: FiniteDuration) {
    def validator(validator: UdpMessage => Validation[CheckResult]) = new UdpSimpleCheck(validator, timeout)
  }
}
