package hska.iwi.telegramBot.commands

import com.redis.RedisClient
import hska.iwi.telegramBot.news.Course
import hska.iwi.telegramBot.news.Course.Course
import hska.iwi.telegramBot.service._
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.models.User
import info.mukel.telegrambot4s.Implicits._
import info.mukel.telegrambot4s.methods.ParseMode
import org.json4s.jackson.Serialization.read

trait Admin extends Commands with Instances with ObjectSerialization {
  _: TelegramBot =>

  onCommand("/list") { implicit msg =>
    {
      using(_.from) { user =>
        if (Admins.allowed.contains(UserID(user.id))) {
          val userIDList: Set[Option[String]] = redis.smembers("users").get
          val users: Set[User] =
            userIDList.flatten.flatMap(userID => redis.get(s"user:$userID").map(read[User]))
          users.foreach(user => reply(user.toString))
        } else {
          reply("Cannot shutdown bot without admin privileges. This incident will be reported!")
          logger.warn(s"User $user tried to shutdown service, but is not an admin")
        }
      }
    }
  }

  onCommand("/shutdown") { implicit msg =>
    {
      using(_.from) { user =>
        if (Admins.allowed.contains(UserID(user.id))) {
          reply(s"Shutting down bot. ${"Bye Bye".italic} 👋", parseMode = ParseMode.Markdown)
          Thread.sleep(2000)
          System.exit(0)
        } else {
          reply("Cannot shutdown bot without admin privileges. This incident will be reported!")
          logger.warn(s"User $user tried to shutdown service, but is not an admin")
        }
      }
    }
  }

  onCommand("/userconfig") { implicit msg =>
    val msgParts = msg.text.get.split(" ")
    val userID = if (msgParts.size > 1) {
      Some(msgParts(1))
    } else {
      None
    }

    userID match {
      case None => logger.warn("No userID received")
      case Some(id) =>
        logger.info(s"User id: $id")
        val redisLookup = RedisInstance.getUserConfigFor(UserID(id.toInt))
        reply(redisLookup.toString)
    }
  }

  onCommand("/setconfig") { implicit msg =>
    val msgParts = msg.text.get.split(" ")
    if (msgParts.size >= 3) {
      val courseAsString = msgParts(1)
      val courseSettingAsString = msgParts(2)

      val user = UserID(msg.from.get.id)
      val course = Course.withNameOpt(courseAsString)

      if (course.isEmpty) {
        reply(s"${courseAsString.italic} is not a valid course")
      } else {
        try {
          val courseSetting = courseSettingAsString.toBoolean
          RedisInstance.setUserConfig(user, course.get, courseSetting)

          reply(s"Set $courseAsString to ${courseSettingAsString.italic}", ParseMode.Markdown)
        } catch {
          case _: Exception => reply(s"${courseSettingAsString.italic} is not a valid setting")
        }
      }

    }

  }

}
