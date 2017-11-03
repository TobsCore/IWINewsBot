package hska.iwi.telegramBot

import java.io.IOException
import java.net.ConnectException

import com.redis.RedisClient
import com.typesafe.scalalogging.Logger
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.Implicits._
import info.mukel.telegrambot4s.methods.ParseMode
import info.mukel.telegrambot4s.models.User

import scala.collection.mutable
import scala.io.Source

class IWINewsBot() extends TelegramBot with Polling with Commands {
  lazy val token: String = scala.util.Properties.envOrNone("BOT_TOKEN").getOrElse(Source.fromFile("bot.token").getLines().mkString)
  override val logger: Logger = Logger[IWINewsBot]
  val subscribedUsers: scala.collection.mutable.HashSet[User] = new mutable.HashSet[User]()
  val redis = new RedisClient("localhost", 6379)

  onCommand('start) { implicit msg =>
    msg.from.foreach(user => {
      try {
        if (redis.sadd("users", user).getOrElse(0l).toInt == 1) {
          reply("You have successfully subscribed to the faculty news.")
          logger.info(s"User ${user} added to subscriptions.")
        } else {
          reply("You already subscribed to the faculty news.")
          logger.info(s"${user} already subscribed")
        }
      }
      catch {
        case rte: RuntimeException => logger.error("Cannot connect to redis server"); logger.debug(rte.getMessage)
      }
    })
  }

  onCommand('stop) { implicit msg =>
    msg.from.foreach(user =>
      try {
        if (redis.srem("users", user).getOrElse(0l).toInt == 1) {
          reply("You will not receive any notifications.")
        } else {
          reply("You're currently not receiving any notifications.")
        }
      }
      catch {
        case rte: RuntimeException => logger.error("Cannot connect to redis server"); logger.debug(rte.getMessage)
      })
  }

}

object IWINewsBot extends App {
  new IWINewsBot().run()
}
