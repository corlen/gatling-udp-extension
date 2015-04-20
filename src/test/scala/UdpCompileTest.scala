import io.gatling.core.Predef._
import io.gatling.core.config.Protocol
import io.gatling.core.scenario.Simulation
import io.gatling.udp.Predef._
import io.gatling.udp.UdpMessage
import org.jboss.netty.handler.codec.string.StringDecoder
import org.jboss.netty.handler.codec.string.StringEncoder
import io.gatling.core.validation.Validation
import io.gatling.core.validation.Failure
import io.gatling.core.check.CheckResult
import io.gatling.core.validation.Success

/**
 * @author carlos.raphael.lopes@gmail.com
 */
class UdpTest extends Simulation {
  
  val udpConfig = udp
    .address("localhost")
    .port(12345)
    .channelHandlers(
        new StringDecoder, 
        new StringEncoder)
        
  val scn = scenario("Udp Compile Test")
    .exec(udp("Connect").connect())
    .exec(
        udp("Sending msg").
        send("udp message...").
        check(udpCheck.within(5).validator({ message: UdpMessage =>
          message.content.asInstanceOf[String].startsWith("test") match {
            case true => CheckResult.NoopCheckResultSuccess
            case false => Failure("KO")
          }
        }))
     )
     .exec(udp("disconnect").disconnect())
      
  setUp(scn.inject(atOnceUsers(100))).protocols(udpConfig)
}