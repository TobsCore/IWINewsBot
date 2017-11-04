package hska.iwi.telegramBot

import akka.actor._
import scala.concurrent.duration._


import com.redis.RedisClient
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import com.typesafe.scalalogging.Logger
import hska.iwi.telegramBot.news.{Entry, FeedReader}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.methods.SendMessage
import info.mukel.telegrambot4s.models.User

import scala.io.Source

class IWINewsBot() extends TelegramBot with Polling with Commands {
  override val logger: Logger = Logger[IWINewsBot]

  // Put the token in file 'bot.token' in the root directly of this project. This will prevent the token from leaking
  lazy val token: String = scala.util.Properties.envOrNone("BOT_TOKEN").getOrElse(Source.fromFile("bot.token").getLines().mkString)

  // The redis instance
  // TODO: Put these values in a config file
  val redis = new RedisClient("localhost", 6379)

  // To correctly serialize case classes
  implicit val formats = Serialization.formats(NoTypeHints)

  // Feedreader for INFB news
  // TODO: Outsource URL
  val infbReader = new FeedReader("http://www.iwi.hs-karlsruhe.de/Intranetaccess/REST/atomfeed/newsbulletinboard/INFB")


  // Use Actor system to check for news periodically.
  val backgroundActorSystem = ActorSystem("BackgroundActorSystem")
  // Start searching 10 seconds after launch and then every 1 minute
  system.scheduler.schedule(10 seconds, 1 minute) {
   // sendPushMessageToSubscribers()
  }

  def sendPushMessageToSubscribers(): Unit = {
    val userIds: Option[Set[Option[String]]] = redis.smembers("users")
    if (userIds.isDefined) {
      val realUserIDs: Set[Long] = userIds.get.filter(_.isDefined).map(_.get).map(_.toLong)
      val content: List[Entry] = infbReader.getEntries()
      realUserIDs.foreach(userID => request(SendMessage(userID, "Push Message")))
    }
  }

  onCommand('start) { implicit msg =>
    msg.from.foreach(user => {
      try {
        if (redis.sadd("users", user.id).getOrElse(0l).toInt == 1) {
          reply("You have successfully subscribed to the faculty news.")
          logger.info(s"User ${user} added to subscriptions.")
        } else {
          reply("You already subscribed to the faculty news.")
          logger.info(s"${user} already subscribed")
        }

        // Update the user data
        redis.set(s"user:${user.id}", write(user))
      }
      catch {
        case rte: RuntimeException => {
          logger.error("Cannot connect to redis server")
          logger.debug(rte.getMessage)
        }
      }
    })
  }

  onCommand('feed) { implicit msg => {
    sendPushMessageToSubscribers()
  }
  }

  onCommand('stop) { implicit msg =>
    msg.from.foreach(user => {
      try {
        if (redis.srem("users", user.id).getOrElse(0l).toInt == 1) {
          reply("You will not receive any notifications.")
        } else {
          reply("You're currently not receiving any notifications.")
        }

        logger.info(s"Deleting user data for $user")
        // Remove the user data from the database
        redis.del(s"user:${user.id}")
      }
      catch {
        case rte: RuntimeException => logger.error("Cannot connect to redis server"); logger.debug(rte.getMessage);
      }
    })
  }

  onCommand('list) { implicit msg => {
    val userIDList: Set[Option[String]] = redis.smembers("users").get
    val users: Set[Option[User]] = userIDList.filter(_.isDefined).map(_.get).map(userID => redis.get(s"user:${userID}").map(read[User](_)))
    users.filter(_.isDefined).map(_.get).foreach(user => reply(user.toString))
  }
  }
}


object IWINewsBot extends App {
  new IWINewsBot().run()
}
