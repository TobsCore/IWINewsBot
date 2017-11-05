package hska.iwi.telegramBot

import akka.actor.ActorSystem
import hska.iwi.telegramBot.news.Course.Course
import hska.iwi.telegramBot.news._
import hska.iwi.telegramBot.service.{RedisInstance, UserID}
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.methods.{ParseMode, SendMessage}

import scala.concurrent.duration._
import scala.language.postfixOps

case class BackgroundFeedSync(token: String) extends TelegramBot with Commands {

  val backgroundActorSystem = ActorSystem("BackgroundActorSystem")

  val feedReader = Map(Course.INFB -> FeedReader(FeedURL.INFB),
                       Course.MKIB -> FeedReader(FeedURL.MKIB),
                       Course.INFM -> FeedReader(FeedURL.INFM))

  val feedProcessor = new FeedProcessor(feedReader)

  def start(): Unit = {
    // Start searching 10 seconds after launch and then every 1 minute
    backgroundActorSystem.scheduler.schedule(1 seconds, 1 minute) {
      val entries: Map[Course, Option[Set[Entry]]] = feedProcessor.receiveNewEntries()
      val subsriptionEntries: Map[UserID, Set[Entry]] = Map() // TODO:
      sendPushMessageToSubscribers()
    }
  }

  private def sendPushMessageToSubscribers(): Unit = {
    RedisInstance.get
      .smembers("users")
      .foreach((userIds: Set[Option[String]]) => {
        val userIdList: Set[Long] = userIds.flatten.map(_.toLong)
        val content: Option[List[Entry]] = feedReader(Course.INFM).receiveEntryList()
        content match {
          case Some(entryList) =>
            if (entryList.nonEmpty) {
              val content: List[String] = entryList.map(entry => EntryFormatter.format(entry))
              userIdList.foreach(userID =>
                request(SendMessage(userID, content.head, parseMode = Some(ParseMode.Markdown))))
            }
          case None => logger.debug("No entries received")
        }
      })
  }
}
