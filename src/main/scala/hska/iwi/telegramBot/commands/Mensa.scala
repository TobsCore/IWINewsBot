package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.mensa.{LocalDate, MensaFormatter, MensaMoltke}
import hska.iwi.telegramBot.news._
import hska.iwi.telegramBot.service.HTTPGet
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.methods.ParseMode
import org.apache.commons.lang3.StringEscapeUtils
import org.json4s.{DefaultFormats, _}
import org.json4s.jackson.JsonMethods

trait Mensa extends Commands {
  _: TelegramBot =>
  implicit val jsonDefaultFormats = DefaultFormats

  onCommand("/mensa") { implicit msg =>
    logger.debug("received command 'mensa'")
    val mensaUrl = FeedURL.mensa + LocalDate.getCurrentDate()
    val content = HTTPGet.get(mensaUrl)
    if (content.isDefined) {
      //parses the json entries and stores them in a MensaMoltke object
      val mensa = JsonMethods.parse(content.get).extract[MensaMoltke]
      reply(MensaFormatter.format(mensa), parseMode = Some(ParseMode.HTML))
    }
  }
}
