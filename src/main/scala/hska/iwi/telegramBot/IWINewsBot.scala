package hska.iwi.telegramBot

// Is used to write syntax such as '10 seconds' in akka calls. Otherwise warnings would be thrown during compilation.
import scala.language.postfixOps
import akka.actor._

import scala.concurrent.duration._
import com.redis.RedisClient
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import com.typesafe.scalalogging.Logger
import hska.iwi.telegramBot.news.{Entry, FeedReader, FeedURL}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.methods.SendMessage
import info.mukel.telegrambot4s.models.User

import scala.io.Source

class IWINewsBot() extends TelegramBot with Polling with Commands with Callbacks {
  override val logger = Logger(getClass)

  // Put the token in file 'bot.token' in the root directly of this project. This will prevent the token from leaking
  lazy val token: String = scala.util.Properties.envOrNone("BOT_TOKEN").getOrElse(Source.fromFile("bot.token").getLines().mkString)

  // The redis instance
  // TODO: Put these values in a config file
  val redis = new RedisClient("localhost", 6379)

  // To correctly serialize case classes
  implicit val formats = Serialization.formats(NoTypeHints)

  // Feedreader for INFB news
  val infbReader = new FeedReader(FeedURL.INFB)
  val mkibReader = new FeedReader(FeedURL.MKIB)
  val infmReader = new FeedReader(FeedURL.INFM)


  // Use Actor system to check for news periodically.
  val backgroundActorSystem = ActorSystem("BackgroundActorSystem")
  // Start searching 10 seconds after launch and then every 1 minute
  system.scheduler.schedule(10 seconds, 1 minute) {
    sendPushMessageToSubscribers()
  }

  def sendPushMessageToSubscribers(): Unit = {
    redis.smembers("users").foreach((userIds: Set[Option[String]]) => {
      val userIdList: Set[Long] = userIds.flatten.map(_.toLong)
      val content: Option[List[Entry]] = infbReader.getEntries()
      content match {
        case Some(entryList) => {
          if (!entryList.isEmpty) {
            val contentBuilder = new StringBuilder
            entryList.foreach(entry => contentBuilder.append(entry.toString).append("\n"))
            val content = contentBuilder.mkString
            userIdList.foreach(userID => request(SendMessage(userID, content)))
          }
        }
        case None => logger.debug("No entries received")
      }
    })
  }

  onCommand("/start") { implicit msg =>
    using(_.from) { user => {
      try {
        if (redis.sadd("users", user.id).getOrElse(0l).toInt == 1) {
          reply("You have successfully subscribed to the faculty news.")
          logger.info(s"User ${user} added to subscriptions.")

          // Set configuration. Everything is set to true, since the user subscribes to all subjects by default.
          redis.hmset(s"config:${user.id}", Map("INFB" -> true, "MKIB" -> true, "INFM" -> true))
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
    }
    }
  }

  onCommand("/abo") { implicit msg => {
    using(_.from) { user =>
      logger.info(s"User $user is changing settings.")
    }
  }
  }

  onCommand("/feed") { implicit msg => {
    sendPushMessageToSubscribers()
  }
  }

  onCommand("/stop") { implicit msg =>
    using(_.from) { user => {
      try {
        if (redis.srem("users", user.id).getOrElse(0l).toInt == 1) {
          reply("You will not receive any notifications.")
          logger.info(s"Deleting user data for $user")

          // Remove the user data from the database
          redis.del(s"user:${user.id}")
          redis.del(s"config:${user.id}")
        } else {
          reply("You're currently not receiving any notifications.")
        }
      }
      catch {
        case rte: RuntimeException => logger.error("Cannot connect to redis server"); logger.debug(rte.getMessage);
      }
    }
    }
  }

  onCommand("/list") { implicit msg => {
    val userIDList: Set[Option[String]] = redis.smembers("users").get
    val users: Set[User] = userIDList.flatten.map(userID => redis.get(s"user:${userID}").map(read[User](_))).flatten
    users.foreach(user => reply(user.toString))
  }
  }
}


object IWINewsBot extends App {
  val bot = new IWINewsBot()
  bot.run()
}
