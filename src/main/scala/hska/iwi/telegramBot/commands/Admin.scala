package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.news.Course
import hska.iwi.telegramBot.service._
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.models.User
import info.mukel.telegrambot4s.Implicits._
import info.mukel.telegrambot4s.methods.ParseMode
import org.json4s.jackson.Serialization.read

trait Admin extends Commands with Instances with ObjectSerialization with Admins {
  _: TelegramBot =>

  onCommand("/list") { implicit msg =>
    {
      using(_.from) { user =>
        if (isAllowed(user)) {
          val userIDList: Set[Option[String]] = redis.smembers("users").get
          val users: Set[User] =
            userIDList.flatten.flatMap(userID => redis.get(s"user:$userID").map(read[User]))
          users.foreach(user => reply(user.toString))
        } else {
          reply("Cannot list all users - This is an admin feature")
          logger.warn(s"User $user tried to list all users")
        }
      }
    }
  }

  onCommand("/shutdown") { implicit msg =>
    {
      using(_.from) { user =>
        if (isAllowed(user)) {
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

  onCommand("/userconfig", "/userConfig") { implicit msg =>
    using(_.from) { user =>
      if (isAllowed(user)) {
        logger.info(s"Returning user Configuration information to user ${msg.from.get}")
        val msgParts = msg.text.get.split(" ")
        val userID = if (msgParts.size > 1) {
          Some(msgParts(1))
        } else {
          None
        }

        val searchUserID: Int = userID match {
          case None =>
            logger.info("Information request for request sender")
            msg.from.get.id
          case Some(id) =>
            logger.info(s"Information request for User id: $id")
            id.toInt
        }

        val redisLookup = RedisInstance.getConfigFor(UserID(searchUserID))
        reply(redisLookup.toString)
      } else {
        reply("Cannot check user configuration - This is an Admin feature")
        logger.warn(s"User $user tried to check the user configuration.")
      }
    }
  }

    }
  }

  onCommand("/setconfig", "/setConfig", "/set") { implicit msg =>
    using(_.from) { user: User =>
      if (isAllowed(user)) {
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
      } else {
        reply("Cannot set configuration - This is an Admin feature for now.")
        logger.warn(s"User $user tried to set configuration.")
      }
    }

  }
}
