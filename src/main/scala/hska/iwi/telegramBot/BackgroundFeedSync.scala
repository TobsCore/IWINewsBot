package hska.iwi.telegramBot

import akka.actor.ActorSystem
import com.redis.RedisClient
import hska.iwi.telegramBot.news._
import hska.iwi.telegramBot.service._
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.api.{TelegramApiException, TelegramBot}
import info.mukel.telegrambot4s.methods.{ParseMode, SendMessage}
import info.mukel.telegrambot4s.models.ChatId

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Failure

/**
  * The background worker is responsible for checking the feed urls for content and notifying
  * users about it. It contains a list of urls, which will be checked.
  *
  * @param token Since the users have to be notified about updates, the token is needed. This is
  *              the bot's token, which should be defined in the bot.token file and be read by
  *              the main class.
  */
case class BackgroundFeedSync(token: String) extends TelegramBot with Commands with Admins {

  val redis = new RedisInstance(new RedisClient(Configuration.redisHost, Configuration.redisPort))
  val backgroundActorSystem = ActorSystem("BackgroundActorSystem")

  val feedProcessor =
    new FeedProcessor(FeedReader(FeedURL.bulletinBoard), FeedReader(FeedURL.facultyNews))

  /**
    * The background sync task is started by calling this method. Since this starts the
    * background tasks, it should be
    * noted, that calling this method multiple times will yield too many calls to the feed's
    * servers and should be avoided.
    */
  def start(): Unit = {
    // Start searching 10 seconds after launch and then every 2 minutes
    backgroundActorSystem.scheduler.schedule(10 seconds, 2 minutes) {
      logger.debug("Loading remote data.")
      val entries = feedProcessor.receiveEntries()
      val facultyNews = feedProcessor.receiveFacultyNews()
      logger.trace(s"Received ${facultyNews.size} faculty news items")
      val newEntries = saveEntries(entries)
      val newFacultyNews = redis.addFacultyNews(facultyNews)
      newFacultyNews.foreach(news =>
        logger.debug(s"""New Faculty News received, with Hash "${news.hashCode4DB()}"
             |${news.title}
             |${news.publicationDate}
             |${news.description}""".stripMargin))
      newEntries.foreach {
        case (course, entrySet) =>
          entrySet.foreach(news => logger.debug(s"""New $course News received
               |${news.id}
               |${news.publicationDate}
               |${news.title}
               |${news.subTitle}
               |${news.content}"""))
      }
      val subscriptionEntries = entriesForSubscribers(newEntries)
      val subscribedFacultyNews = subscribedFacultyNewsUsers()
      logger.trace(s"Received ${subscribedFacultyNews.size} faculty news subscribers")
      sendPushMessageToSubscribers(subscriptionEntries)
      sendFacultyNewsToSubscribers(subscribedFacultyNews, newFacultyNews)
    }
  }

  /**
    * In order to send the messages to the users that subsribed to the messages, each user is
    * mapped to the news messages, that arrived. This is done by reading the users configuration
    * and then selecting only the relevant entries to a set. Duplicates are eliminated, so if a
    * user subscribed to both mkib and infb news and a new entry is posted for both infb and
    * mkib, the subscribed users will only receive one message and not two.
    *
    * @param entries A map, which maps the course to a set of entries. The set with the entries
    *                can be empty.
    * @return A Map, where each users id is mapped to a set of new entries. This
    */
  def entriesForSubscribers(entries: Map[Course, Set[Entry]]): Map[UserID, Set[Entry]] = {
    val userConfig = redis.userConfig().filter(_._2.isDefined).mapValues(_.get)
    userConfig.map {
      case (userID: UserID, subscribedCourses: Set[Course]) =>
        val coursesForUser = mutable.Set.empty[Entry]
        entries
          .filter(e => subscribedCourses.contains(e._1))
          .foreach(e => e._2.foreach(entry => coursesForUser += entry))
        (userID, coursesForUser.toSet)
    }
  }

  def subscribedFacultyNewsUsers(): Set[UserID] = {
    val userConfig: Map[UserID, Option[Boolean]] = redis.getFacultyConfig
    userConfig.filter(_._2.isDefined).mapValues(_.get).filter(_._2).keys.toSet
  }

  def saveEntries(allEntries: Map[Course, Set[Entry]]): Map[Course, Set[Entry]] = {
    allEntries.map((f: (Course, Set[Entry])) => f._1 -> redis.addNewsEntries(f._1, f._2))
  }

  def sendPushMessageToSubscribers(userToEntryMap: Map[UserID, Set[Entry]]): Unit = {
    for ((userID, entrySet) <- userToEntryMap) {
      for (entry <- entrySet) {
        trySendMessage(ChatId(userID.id), entry.toString)
      }
    }
  }

  def trySendMessage(chatID: ChatId, content: String): Unit = {
    request(SendMessage(chatID, content, parseMode = Some(ParseMode.HTML)))
      .onComplete {
        case Failure(telegramException: TelegramApiException) =>
          telegramException.errorCode match {
            case 439 =>
              logger.error(
                s"Received a 439 error [Too many requests] while trying to send message " +
                  s"to user with $chatID")
            case 403 =>
              logger.error(
                s"Blocked by user: User with id $chatID has forbidden access, which " +
                  s"caused an error. The message could not be sent.")
            case e =>
              logger.error(s"Unknown error occured, with error-code $e. Better look into this.")
          }
        case _ => logger.debug(s"Sent message to user $chatID")
      }
  }

  def sendFacultyNewsToSubscribers(subscribedUsers: Set[UserID], news: List[FacultyNews]): Unit = {
    subscribedUsers.foreach(userID => {
      for (entry <- news) {
        trySendMessage(ChatId(userID.id), entry.toString)
      }
    })
  }

}
