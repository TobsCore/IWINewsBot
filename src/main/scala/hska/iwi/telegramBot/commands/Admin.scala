package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.service.{Instances, ObjectSerialization}
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.models.User
import org.json4s.jackson.Serialization.read

trait Admin extends Commands with Instances with ObjectSerialization {
  _: TelegramBot =>

  onCommand("/list") { implicit msg => {
    val userIDList: Set[Option[String]] = redis.smembers("users").get
    val users: Set[User] = userIDList.flatten.flatMap(userID => redis.get(s"user:$userID").map(read[User]))
    users.foreach(user => reply(user.toString))
  }
  }

}
