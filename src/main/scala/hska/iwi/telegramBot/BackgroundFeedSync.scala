package hska.iwi.telegramBot

import akka.actor.ActorSystem
import com.redis.RedisClient
import hska.iwi.telegramBot.news.Course.Course
import hska.iwi.telegramBot.news._
import hska.iwi.telegramBot.service.{Configuration, RedisInstance, UserID}
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.methods.{ParseMode, SendMessage}
import info.mukel.telegrambot4s.models.ChatId

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * The background worker is responsible for checking the feed urls for content and notifying
  * users about it. It contains a list of urls, which will be checked.
  *
  * @param token Since the users have to be notified about updates, the token is needed. This is
  *              the bot's token, which should be defined in the bot.token file and be read by
  *              the main class.
  */
case class BackgroundFeedSync(token: String) extends TelegramBot with Commands {

  val redis = new RedisInstance(new RedisClient(Configuration.redisHost, Configuration.redisPort))
  val backgroundActorSystem = ActorSystem("BackgroundActorSystem")

  val feedReader = Map(Course.INFM -> FeedReader(FeedURL.bulletinBoard))

  val feedProcessor = new FeedProcessor(feedReader)

  def entriesForSubscribers(
      entries: Map[Course, Option[Set[Entry]]],
      userConfig: Map[UserID, Option[Set[Course]]]): Map[UserID, Set[Entry]] = {
    // TODO: Implement Method
    Map()
  }

  def saveEntries(allEntries: Map[Course, Option[Set[Entry]]]): Map[Course, Option[Set[Entry]]] = {
    allEntries.map((f: (Course, Option[Set[Entry]])) =>
      f._1 -> redis.addNewsEntries(f._1, f._2.getOrElse(Set())))
  }

  /**
    * The background sync task is started by calling this method. Since this starts the
    * background tasks, it should be
    * noted, that calling this method multiple times will yield too many calls to the feed's
    * servers and should be avoided.
    */
  def start(): Unit = {
    // Start searching 10 seconds after launch and then every 1 minute
    backgroundActorSystem.scheduler.schedule(10 seconds, 1 minute) {
      logger.debug("Loading remote data.")
      val entries = feedProcessor.receiveEntries()
      val userConfig = redis.userConfig()
      val newEntries = saveEntries(entries)
      val subscriptionEntries = entriesForSubscribers(entries, userConfig)
      sendPushMessageToSubscribers(newEntries)
    }
  }

  private def sendPushMessageToSubscribers(newEntries: Map[Course, Option[Set[Entry]]]): Unit = {
    newEntries
      .filterKeys(_.equals(Course.INFM))
      .foreach((f: (Course, Option[Set[Entry]])) => {

        val entryList = f._2.getOrElse(Set())
        entryList.foreach((entry: Entry) => {
          redis.getAllUserIDs
            .getOrElse(Set())
            .foreach(userID => {
              logger.debug(s"Sending reply to user $userID")
              request(
                SendMessage(ChatId(userID.id), entry.toString, parseMode = Some(ParseMode.HTML)))
            })
        })
      })
  }

}
