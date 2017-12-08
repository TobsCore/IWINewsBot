package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.news.{INFB, INFM, MKIB}
import hska.iwi.telegramBot.service.{Instances, UserID}
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands

trait Subscription extends Commands with Instances {
  _: TelegramBot =>

  onCommand("/start") { implicit msg =>
    using(_.from) { user =>
      {
        val userID = UserID(user.id)
        try {
          if (redis.addUser(userID)) {
            reply(
              """Du erhältst ab jetzt alle Nachrichten des schwarzen Bretts und der Fakultät IWI an der HSKA.
              |Um Deine Einstellungen anzupassen, wähle /abo aus.""".stripMargin)
            logger.info(s"${user.firstName} ${user.lastName.getOrElse("")} added to subscriptions.")
            logger.debug(s"$user is stored in Database")

            // Set configuration. Everything is set to true, since the user subscribes to all
            // subjects by default.
            redis.setUserConfig(userID, Map(INFB -> true, MKIB -> true, INFM -> true))
          } else {
            reply("Du erhälst bereits Nachrichten.")
          }

          // Update the user data
          redis.setUserData(userID, user)
          redis.setFacultyConfigForUser(userID, configValue = true)
        } catch {
          case rte: RuntimeException =>
            logger.error("Cannot connect to redis server")
            logger.debug(rte.getMessage)
        }
      }
    }
  }

  onCommand("/stop") { implicit msg =>
    using(_.from) { user =>
      {
        val userID = UserID(user.id)
        try {
          if (redis.removeUser(userID)) {
            reply("Du erhältst von nun an keine Nachrichten mehr.")
            logger.info(s"User ${user.firstName} ${user.lastName.getOrElse("")} unsubscribed")
            logger.debug(s"Deleting user data for $user")

            // Remove the user data from the database
            redis.removeUserData(userID)
            redis.removeUserConfig(userID)
            redis.removeFacultyConfigForUser(userID)
          } else {
            reply("Du bist bereits abgemeldet.")
          }
        } catch {
          case rte: RuntimeException =>
            logger.error("Cannot connect to redis server")
            logger.debug(rte.getMessage);
        }
      }
    }
  }

}
