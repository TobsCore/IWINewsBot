package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.service.{Admins, Instances, ObjectSerialization, UserID}
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

}
