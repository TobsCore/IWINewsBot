package hska.iwi.telegramBot

// Is used to write syntax such as '10 seconds' in akka calls. Otherwise warnings would be thrown during compilation.
import akka.actor._
import hska.iwi.telegramBot.commands.{AboSettings, Admin, Subscription}
import hska.iwi.telegramBot.news.{Entry, FeedReader, FeedURL}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.SendMessage

import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps

class IWINewsBot()
  extends TelegramBot
    with Polling
    with Commands
    with Callbacks

    with Subscription
    with Admin
    with AboSettings {

  // Put the token in file 'bot.token' in the root directly of this project. This will prevent the token from leaking
  lazy val token: String = scala.util.Properties.envOrNone("BOT_TOKEN").getOrElse(Source.fromFile("bot.token").getLines().mkString)

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
        case Some(entryList) =>
          if (entryList.nonEmpty) {
            val contentBuilder = new StringBuilder
            entryList.filter(_.id == "NewsBulletinBoard:919").foreach(entry => contentBuilder.append(entry.toString).append("\n"))
            val content = contentBuilder.mkString
            userIdList.foreach(userID => request(SendMessage(userID, content)))
          }
        case None => logger.debug("No entries received")
      }
    })
  }

}


object IWINewsBot extends App {
  val bot = new IWINewsBot()
  bot.run()
}
