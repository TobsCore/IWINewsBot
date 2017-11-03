package hska.iwi.telegramBot

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

  onCommand('start) { implicit msg =>
    msg.from.foreach(user =>
      if (subscribedUsers.add(user)) {
        reply("You have successfully subscribed to the faculty news.")
        logger.info(s"User ${user} added to subscriptions.")
      } else {
        reply("You already subscribed to the faculty news.")
        logger.info(s"${user} already subscribed")
      }
    )
  }

  onCommand('stop) { implicit msg =>
    msg.from.foreach(user =>
    if (subscribedUsers.remove(user)) {
      reply("You will not receive any notifications.")
    } else {
      reply("You're currently not receiving any notifications.")
    })
  }

}

object IWINewsBot extends App {
  new IWINewsBot().run()
}
