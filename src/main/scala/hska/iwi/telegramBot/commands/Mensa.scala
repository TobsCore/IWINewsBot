package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.mensa.MensaMoltke
import hska.iwi.telegramBot.news._
import hska.iwi.telegramBot.service.{FeedURL, HTTPGet, LocalDateTime}
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.methods.ParseMode
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, _}

trait Mensa extends Commands {
  _: TelegramBot =>
  implicit val jsonDefaultFormats = DefaultFormats

  onCommand("/mensa") { implicit msg =>
    logger.debug(s"${msg.from.getOrElse("")} requested mensa data")
    val mensaUrl = FeedURL.mensa + LocalDateTime.getCurrentDate()
    val content = HTTPGet.get(mensaUrl)
    if (content.isDefined) {
      //parses the json entries and stores them in a MensaMoltke object
      val mensa = JsonMethods.parse(content.get).extract[MensaMoltke]
      reply(mensa.toString, parseMode = Some(ParseMode.HTML))
    }
  }
}
