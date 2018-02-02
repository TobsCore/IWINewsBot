package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.mensa.MensaMoltke
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
  implicit val jsonDefaultFormats: DefaultFormats.type = DefaultFormats

  val daysToAdd: Array[Int] = new Array[Int](5)

  onCommand("/mensa") { implicit msg =>
    logger.debug(s"${msg.from.getOrElse("")} requested mensa data")
    logger.debug("received command 'mensa'")
    reply("Für welchen Tag soll das Mensaangebot ausgegeben werden?",
          replyMarkup = Some(createInlineKeyboardMarkup()))
  }

  def createInlineKeyboardMarkup(): InlineKeyboardMarkup = {
    setDaysToAddArray()

    val mensaToday =
      InlineKeyboardButton.callbackData(LocalDateTime.formatPrettyDateInFuture(daysToAdd(0)),
                                        tagMensa("0"))
    val mensaTomorrow =
      InlineKeyboardButton.callbackData(LocalDateTime.formatPrettyDateInFuture(daysToAdd(1) + 1),
                                        tagMensa("1"))
    val mensaDayAfter =
      InlineKeyboardButton.callbackData(LocalDateTime.formatPrettyDateInFuture(daysToAdd(2) + 2),
                                        tagMensa("2"))
    val mensaDay4 =
      InlineKeyboardButton.callbackData(LocalDateTime.formatPrettyDateInFuture(daysToAdd(3) + 3),
                                        tagMensa("3"))

    val mensaDay5 =
      InlineKeyboardButton.callbackData(LocalDateTime.formatPrettyDateInFuture(daysToAdd(4) + 4),
                                        tagMensa("4"))

    val mensaDays =
      Seq[InlineKeyboardButton](mensaToday, mensaTomorrow, mensaDayAfter, mensaDay4, mensaDay5)
    InlineKeyboardMarkup.singleColumn(mensaDays)
  }

  onCallbackWithTag("Mensa") { implicit cbq: CallbackQuery =>
    val mensaDayID = cbq.data.get.toInt
    val daysInFuture = daysToAdd(mensaDayID) + mensaDayID

    val mensaUrl = FeedURL.mensa + LocalDateTime.getDateInFuture(daysInFuture)
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

  def tagMensa: String => String = prefixTag("Mensa")

  def setDaysToAddArray(): Unit = {
    var found = false
    if (LocalDateTime.getWeekDayPlusBonusDays(0) == 6) {
      daysToAdd(0) = 2
      daysToAdd(1) = 2
      daysToAdd(2) = 2
      daysToAdd(3) = 2
      daysToAdd(4) = 2
      found = true
    } else if (LocalDateTime.getWeekDayPlusBonusDays(0) == 7) {
      daysToAdd(0) = 1
      daysToAdd(1) = 1
      daysToAdd(2) = 1
      daysToAdd(3) = 1
      daysToAdd(4) = 1
      found = true
    }

    if (LocalDateTime.getWeekDayPlusBonusDays(1) == 6 && !found) {
      daysToAdd(0) = 0
      daysToAdd(1) = 2
      daysToAdd(2) = 2
      daysToAdd(3) = 2
      daysToAdd(4) = 2
      found = true
    } else if (LocalDateTime.getWeekDayPlusBonusDays(1) == 7 && !found) {
      daysToAdd(0) = 0
      daysToAdd(1) = 1
      daysToAdd(2) = 1
      daysToAdd(3) = 1
      daysToAdd(4) = 1
      found = true
    }

    if (LocalDateTime.getWeekDayPlusBonusDays(2) == 6 && !found) {
      daysToAdd(0) = 0
      daysToAdd(1) = 0
      daysToAdd(2) = 2
      daysToAdd(3) = 2
      daysToAdd(4) = 2
      found = true
    } else if (LocalDateTime.getWeekDayPlusBonusDays(2) == 7 && !found) {
      daysToAdd(0) = 0
      daysToAdd(1) = 0
      daysToAdd(2) = 1
      daysToAdd(3) = 1
      daysToAdd(4) = 1
      found = true
    }

    if (LocalDateTime.getWeekDayPlusBonusDays(3) == 6 && !found) {
      daysToAdd(0) = 0
      daysToAdd(1) = 0
      daysToAdd(2) = 0
      daysToAdd(3) = 2
      daysToAdd(4) = 2
      found = true
    } else if (LocalDateTime.getWeekDayPlusBonusDays(3) == 7 && !found) {
      daysToAdd(0) = 0
      daysToAdd(1) = 0
      daysToAdd(2) = 0
      daysToAdd(3) = 1
      daysToAdd(4) = 1
      found = true
    }

    if (LocalDateTime.getWeekDayPlusBonusDays(4) == 6 && !found) {
      daysToAdd(0) = 0
      daysToAdd(1) = 0
      daysToAdd(2) = 0
      daysToAdd(3) = 0
      daysToAdd(4) = 2
      found = true
    } else if (LocalDateTime.getWeekDayPlusBonusDays(4) == 7 && !found) {
      daysToAdd(0) = 0
      daysToAdd(1) = 0
      daysToAdd(2) = 0
      daysToAdd(3) = 0
      daysToAdd(4) = 1
      found = true
    }
  }
}
