package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.news.Course
import hska.iwi.telegramBot.service.{RedisInstance, UserID}
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands

trait Subscription extends Commands {
  _: TelegramBot =>

  onCommand("/start") { implicit msg =>
    using(_.from) { user =>
      {
        val userID = UserID(user.id)
        try {
          if (RedisInstance.addUser(userID)) {
            reply("You have successfully subscribed to the faculty news.")
            logger.info(s"User $user added to subscriptions.")

            // Set configuration. Everything is set to true, since the user subscribes to all
            // subjects by default.
            RedisInstance.setUserConfig(
              userID,
              Map(Course.INFB -> true, Course.MKIB -> true, Course.INFM -> true))
          } else {
            reply("You already subscribed to the faculty news.")
            logger.info(s"$user already subscribed")
          }

          // Update the user data
          RedisInstance.setUserData(userID, user)
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
          if (RedisInstance.removeUser(userID)) {
            reply("You will not receive any notifications.")
            logger.info(s"Deleting user data for $user")

            // Remove the user data from the database
            RedisInstance.removeUserData(userID)
            RedisInstance.removeUserConfig(userID)
          } else {
            reply("You're currently not receiving any notifications.")
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
