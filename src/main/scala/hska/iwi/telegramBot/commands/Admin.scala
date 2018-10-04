package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.BotFunctions.SafeSendMessage
import hska.iwi.telegramBot.news.Course
import hska.iwi.telegramBot.service._
import info.mukel.telegrambot4s.Implicits._
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.methods.ParseMode
import info.mukel.telegrambot4s.models.{ChatId, User}
import scala.concurrent.duration._
import scala.language.postfixOps

trait Admin
    extends Commands
    with Instances
    with ObjectSerialization
    with Admins
    with SafeSendMessage {
  _: TelegramBot =>

  onCommand("/admin") { implicit msg =>
    {
      using(_.from) { user =>
        if (isAllowed(user)) {
          reply("""Admin Functions:
            |
            |/list - Lists all subscribed users
            |/subs - Lists all users for subscribed information channels (MKIB, etc.)
            |/userconfig userID - Gets the userID's configuration
            |/announce - Send announcement to all users
            |
            |/shutdown - Shuts down bot
          """.stripMargin)
        } else {
          reply("Cannot list users - This is an admin feature")
          logger.warn(s"User $user tried to list all users")
        }
      }
    }
  }

  onCommand("/list") { implicit msg =>
    {
      using(_.from) { user =>
        if (isAllowed(user)) {
          reply(s"Total of ${redis.getAllUserIDs.getOrElse(Set()).size.toString.bold} users are " +
                  s"subscribed",
                parseMode = ParseMode.Markdown)

          val s: StringBuilder = new StringBuilder()
          var counter = 1
          redis.getAllUserIDs
            .getOrElse(Set())
            .map(userID => redis.getUserData(userID))
            .foreach(userOption =>
              userOption.foreach(user => {
                s.append(
                  "%d. %s %s | Username: %s | [%d]".format(
                    counter,
                    user.firstName,
                    user.lastName.getOrElse(""),
                    user.username.getOrElse("<i>not defined</i>"),
                    user.get.id))
                s.append("\n")
                counter += 1
              }))
          val splitted = s.toString().split("40.|80.|120.|160.")
          for (s <- splitted) {
            reply(s.toString, parseMode = ParseMode.HTML)
          }
        } else {
          reply("Cannot list users - This is an admin feature")
          logger.warn(s"User $user tried to list all users")
        }
      }
    }
  }

  onCommand("/shutdown") { implicit msg =>
    {
      using(_.from) { user =>
        if (isAllowed(user)) {
          logger.info(s"$user is shutting down bot.")
          reply(s"Shutting down bot. ${"Bye Bye".italic} 👋", parseMode = ParseMode.Markdown)
          Thread.sleep(1000)
          System.exit(0)
        } else {
          reply("Cannot shutdown bot without admin privileges. This incident will be reported!")
          logger.warn(s"User $user tried to shutdown service, but is not an admin")
        }
      }
    }
  }

  onCommand("/announce", "/announcement") { implicit msg =>
    {
      using(_.from) { user =>
        if (isAllowed(user)) {
          logger.info(s"$user is sending announcement.")
          val announcement = msg.text.getOrElse("").split("\\s+", 2).lift(1).getOrElse("")
          logger.info(s"Announcement: $announcement")
          if (announcement.isEmpty) {
            reply("Failed! Cannot send empty announcement.")
            logger.warn("Received empty announcement message. Will not send it to users.")
          } else {
            logger.debug(s"Message: $announcement")

            val users = redis.getAllUserIDs
              .getOrElse(Set())
            for (user <- users) {
              trySendMessage(ChatId(user.id), announcement)
              Thread.sleep((50 millis).toMillis)
            }
          }
        } else {
          logger.warn(s"User $user tried to send announcement service, but is not an admin")
        }
      }
    }
  }

  onCommand("/userconfig", "/userConfig") { implicit msg =>
    using(_.from) { user =>
      if (isAllowed(user)) {
        logger.info(
          s"Admin Function: Returning user Configuration information to user ${msg.from.get}")
        val msgParts = msg.text.get.split(" ")
        val userID = if (msgParts.size > 1) {
          Some(msgParts(1))
        } else {
          None
        }

        val searchUserID: Int = userID match {
          case None =>
            logger.debug("Information request for request sender")
            msg.from.get.id
          case Some(id) =>
            logger.debug(s"Information request for User id: $id")
            id.toInt
        }

        val redisLookup = redis.getConfigFor(UserID(searchUserID))
        reply(redisLookup.toString)
      } else {
        reply("Cannot check user configuration - This is an Admin feature")
        logger.warn(s"User $user tried to check the user configuration.")
      }
    }
  }

  onCommand("/subscriptions", "/subs") { implicit msg =>
    using(_.from) { user: User =>
      if (isAllowed(user)) {
        val subscriptions = redis.getConfigForUsers
        subscriptions
          .foreach(e => reply(s"${e._1} ${e._2.size}: ${e._2.toString}"))
      } else {
        reply("Cannot check subscriptions - This is an Admin feature")
        logger.warn(s"User $user tried to check all subscriptions")
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
          val course = Course.getCourseByName(courseAsString)

          if (course.isEmpty) {
            reply(s"${courseAsString.italic} is not a valid course")
          } else {
            try {
              val courseSetting = courseSettingAsString.toBoolean
              redis.setUserConfig(user, course.get, courseSetting)

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
