package io.gatling.udp

import org.jboss.netty.channel.Channel
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import io.gatling.core.akka.BaseActor
import io.gatling.core.check.CheckResult
import io.gatling.core.result.writer.DataWriterClient
import io.gatling.core.session.Session
import io.gatling.core.util.TimeHelper.nowMillis
import io.gatling.core.result.writer.DataWriter
import scala.collection.mutable
import io.gatling.core.validation.Failure
import io.gatling.core.check.Check
import io.gatling.core.util.TimeHelper._
import io.gatling.core.result.message.{ Status, KO, OK }
import java.util.concurrent.Executors
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory
import org.jboss.netty.bootstrap.ConnectionlessBootstrap
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.ChannelPipeline
import org.jboss.netty.channel.Channels
import java.net.InetSocketAddress

/**
 * @author carlos.raphael.lopes@gmail.com
 */
class UdpTrackerActor extends BaseActor with DataWriterClient {
  
  val nioThreadPool = Executors.newCachedThreadPool
  val nioDatagramChannelFactory = new NioDatagramChannelFactory(nioThreadPool, Runtime.getRuntime.availableProcessors())
  
  // messages to be tracked through this HashMap - note it is a mutable hashmap
  val sentMessages = mutable.HashMap.empty[String, (Long, UdpCheck, Session, ActorRef, String)]
  val receivedMessages = mutable.HashMap.empty[String, (Long, UdpMessage)]
  
  override def postStop(): Unit = {
    nioThreadPool.shutdown()
  }
  
  override def receive: Receive = initialState
  
  val initialState: Receive = {
    case Connect(tx) => {
      val connectionBootstrap = new ConnectionlessBootstrap(nioDatagramChannelFactory)
      connectionBootstrap.setPipelineFactory(new ChannelPipelineFactory {
          override def getPipeline: ChannelPipeline = {
            val pipeline = Channels.pipeline()
            tx.protocol.handlers.zipWithIndex.foreach{ case (handler, i) => {
                pipeline.addLast("ChannelHandler" + i, handler)}          
            }
            pipeline.addLast("handler", new MessageListener(tx, self))
            pipeline
          }
      })
      
      val future = connectionBootstrap.connect(new InetSocketAddress(tx.protocol.address, tx.protocol.port)).awaitUninterruptibly()
      
      if (future.isSuccess) {
        val newSession = tx.session.set("udpTracker", self)
        val newTx = tx.copy(session = newSession)
        context.become(connectedState(future.getChannel, connectionBootstrap, newTx))
        tx.next ! newSession
      } else {
        throw new RuntimeException
      }
    }
  }
  
  def connectedState(channel: Channel, connectionBootstrap: ConnectionlessBootstrap, tx: UdpTx): Receive = {
    def failPendingCheck(udpCheck: UdpCheck, tx: UdpTx, message: String, sentTime: Long, receivedTime: Long): UdpTx = {
      val newSession = tx.session.markAsFailed
      val newTx = tx.copy(session = newSession)
      
      udpCheck match {
        case c: UdpCheck =>
          logRequest(tx.session, tx.requestName, KO, sentTime, receivedTime, Some(message))
          tx.next ! newSession
          context.become(connectedState(channel, connectionBootstrap, newTx))
          newTx
          
        case _ => newTx
      }
    }
    
    def succeedPendingCheck(udpCheck: UdpCheck, checkResult: CheckResult, sentTime: Long, receivedTime: Long) = {
      udpCheck match {
        case check: UdpCheck =>
          logRequest(tx.session, tx.requestName, OK, tx.start, nowMillis)
          
          val newUpdates = if (checkResult.hasUpdate) {
            checkResult.update.getOrElse(Session.Identity) :: tx.updates
          } else {
            tx.updates
          }
          // apply updates and release blocked flow
          val newSession = tx.session.update(newUpdates)

          tx.next ! newSession
          val newTx = tx.copy(session = newSession, updates = Nil)
          context.become(connectedState(channel, connectionBootstrap, newTx))
        case _ =>
      }
    }
    
    {
      // message was sent; add the timestamps to the map
      case MessageSend(requestName, check, message, session, next) =>
        val sendMessage = message.getOrElse(session("udpMessage").validate[UdpMessage].get)
        val start = nowMillis
        
        check match {
          case c: UdpCheck =>
            // do this immediately instead of self sending a Listen message so that other messages don't get a chance to be handled before
            setCheck(tx, channel, requestName, c, next, session, connectionBootstrap, start)
          case _ => next ! session
        }
        
        channel.write(sendMessage.content)
        
        tx.requestName = requestName
        val sentMessage = (start, check, session, next, requestName)
        sentMessages += session.userId -> sentMessage
        
      // message was received; publish to the datawriter and remove from the hashmap
      case MessageReceived(requestId, received, message) =>
        sentMessages.get(requestId) match {
          case Some((startSend, check, session, next, requestName)) =>
            logger.debug(s"Received text message on  :$message")
            
            implicit val cache = scala.collection.mutable.Map.empty[Any, Any]
    
            check.check(message, tx.session) match {
              case io.gatling.core.validation.Success(result) =>
                succeedPendingCheck(check, result, startSend, received)

              case s => failPendingCheck(check, tx, s"check failed $s", startSend, received)
            }
            sentMessages -= requestId
            
            val receivedMessage = (received, message)
            receivedMessages += requestId -> receivedMessage
          
          case None =>
            val receivedMessage = (received, message)
            receivedMessages += requestId -> receivedMessage
        }
      
      case CheckTimeout(check, sentTime) =>
        check match {
          case timeout: UdpCheck =>
            val newTx = failPendingCheck(check, tx, "Check failed: Timeout", sentTime, nowMillis)
            context.become(connectedState(channel, connectionBootstrap, newTx))

            // release blocked session
            newTx.next ! newTx.applyUpdates(newTx.session).session
          case _ =>
        }
        
      case Disconnect(requestName, next, session) => {
        logger.debug(s"Disconnect channel for session: $session")
        channel.close()
        connectionBootstrap.releaseExternalResources()
        val newSession: Session = session.remove("channel")

        next ! newSession
      }
    }
  }
  
  private def logRequest(session: Session, requestName: String, status: Status, started: Long, ended: Long, errorMessage: Option[String] = None): Unit = {
    writeRequestData(
      session,
      requestName,
      started,
      ended,
      ended,
      ended,
      status,
      errorMessage)
  }   
  
  def setCheck(tx: UdpTx, channel: Channel, requestName: String, check: UdpCheck, next: ActorRef, 
      session: Session, connectionBootstrap: ConnectionlessBootstrap, sentTime: Long): Unit = {

    logger.debug(s"setCheck timeout=${check.timeout}")

    // schedule timeout
    scheduler.scheduleOnce(check.timeout) {
      self ! CheckTimeout(check, sentTime)
    }

    val newTx = tx
      .applyUpdates(session)
      .copy(start = nowMillis, next = next, requestName = requestName)
    context.become(connectedState(channel, connectionBootstrap, newTx))
  }
}