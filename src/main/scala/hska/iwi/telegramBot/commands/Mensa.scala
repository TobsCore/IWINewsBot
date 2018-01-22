package hska.iwi.telegramBot.commands

import java.util.Calendar

import hska.iwi.telegramBot.lecturers.Lecturer
import hska.iwi.telegramBot.mensa.MensaMoltke
import hska.iwi.telegramBot.news._
import hska.iwi.telegramBot.service.{FeedURL, HTTPGet, LocalDateTime}
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.methods.{ParseMode, SendMessage}
import info.mukel.telegrambot4s.models.{
  CallbackQuery,
  ChatId,
  InlineKeyboardButton,
  InlineKeyboardMarkup
}
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, _}

trait Mensa extends Commands with Callbacks {
  _: TelegramBot =>
  implicit val jsonDefaultFormats = DefaultFormats

  onCommand("/mensa") { implicit msg =>
    logger.debug(s"${msg.from.getOrElse("")} requested mensa data")
    logger.debug("received command 'mensa'")
    reply("Für welchen Tag soll das Mensaangebot ausgegeben werden?",
          replyMarkup = Some(createInlineKeyboardMarkup()))
  }

  def createInlineKeyboardMarkup(): InlineKeyboardMarkup = {
    //val today = LocalDateTime.getWeekDayFromDate(Calendar.getInstance().getTime())

    val mensaToday =
      InlineKeyboardButton.callbackData(LocalDateTime.formatPrettyCurrentDate(), tagMensa("0"))
    val mensaTomorrow =
      InlineKeyboardButton.callbackData(LocalDateTime.formatPrettyDateInFuture(1), tagMensa("1"))
    val mensaDayAfter =
      InlineKeyboardButton.callbackData(LocalDateTime.formatPrettyDateInFuture(2), tagMensa("2"))

    val mensaDays = Seq[InlineKeyboardButton](mensaToday, mensaTomorrow, mensaDayAfter)
    InlineKeyboardMarkup.singleColumn(mensaDays)
  }

  onCallbackWithTag("Mensa") { implicit cbq: CallbackQuery =>
    val mensaDayID = cbq.data.get.toInt
    var mensaUrl = FeedURL.mensa
    var daysInFuture: Int = 0

    //TODO days of future richtig setzen

    mensaDayID match {
      case 0 => {
        mensaUrl += LocalDateTime.getCurrentDate()
        daysInFuture = 0
      }
      case 1 => {
        mensaUrl += LocalDateTime.getDateInFuture(1)
        daysInFuture = 1
      }
      case 2 => {
        mensaUrl += LocalDateTime.getDateInFuture(2)
        daysInFuture = 2
      }
    }
    val content = HTTPGet.get(mensaUrl)
    if (content.isDefined) {
      ackCallback()(cbq)
      //parses the json entries and stores them in a MensaMoltke object
      val mensa = JsonMethods.parse(content.get).extract[MensaMoltke]
      request(
        SendMessage(ChatId(cbq.message.get.chat.id),
                    mensa.toString(daysInFuture),
                    parseMode = Some(ParseMode.HTML)))
    }
  }

  def tagMensa: String => String = prefixTag("Mensa") _

}
