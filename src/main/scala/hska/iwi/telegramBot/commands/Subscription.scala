package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.service.{Instances, ObjectSerialization}
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import org.json4s.jackson.Serialization.write

trait Subscription extends Commands with Instances with ObjectSerialization {
  _: TelegramBot =>

  onCommand("/start") { implicit msg =>
    using(_.from) { user => {
      try {
        if (redis.sadd("users", user.id).getOrElse(0l).toInt == 1) {
          reply("You have successfully subscribed to the faculty news.")
          logger.info(s"User $user added to subscriptions.")

          // Set configuration. Everything is set to true, since the user subscribes to all subjects by default.
          redis.hmset(s"config:${user.id}", Map("INFB" -> true, "MKIB" -> true, "INFM" -> true))
        } else {
          reply("You already subscribed to the faculty news.")
          logger.info(s"$user already subscribed")
        }

        // Update the user data
        redis.set(s"user:${user.id}", write(user))
      }
      catch {
        case rte: RuntimeException =>
          logger.error("Cannot connect to redis server")
          logger.debug(rte.getMessage)
      }
    }
    }
  }


  onCommand("/stop") { implicit msg =>
    using(_.from) { user => {
      try {
        if (redis.srem("users", user.id).getOrElse(0l).toInt == 1) {
          reply("You will not receive any notifications.")
          logger.info(s"Deleting user data for $user")

          // Remove the user data from the database
          redis.del(s"user:${user.id}")
          redis.del(s"config:${user.id}")
        } else {
          reply("You're currently not receiving any notifications.")
        }
      }
      catch {
        case rte: RuntimeException =>
          logger.error("Cannot connect to redis server")
          logger.debug(rte.getMessage);
      }
    }
    }
  }

}
