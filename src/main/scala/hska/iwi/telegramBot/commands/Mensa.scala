package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.mensa.MensaMoltke
import hska.iwi.telegramBot.service._
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.methods.{EditMessageText, ParseMode}
import info.mukel.telegrambot4s.models._
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, _}

import scala.annotation.switch

trait Mensa extends Commands with Callbacks with Instances {
  _: TelegramBot =>
  implicit val jsonDefaultFormats: DefaultFormats.type = DefaultFormats

  onCommand("/mensa") { implicit msg =>
    logger.info(s"${msg.from.getOrElse("")} requested mensa data")
    reply("Für welchen Tag soll das Mensaangebot ausgegeben werden?",
          replyMarkup = Some(createInlineKeyboardMarkup()))
  }

  def createInlineKeyboardMarkup(): InlineKeyboardMarkup = {
    val daysToAdd = setDaysToAddArray()

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
    ackCallback()(cbq)

    val mensaDayID = cbq.data.get.toInt
    val daysToAdd = setDaysToAddArray()
    val daysInFuture = daysToAdd(mensaDayID) + mensaDayID

    val messageId = cbq.message.get.messageId
    val chatId = ChatId(cbq.message.get.chat.id)

    val userId = UserID(cbq.from.id)
    val priceConfig = redis.getPriceConfigForUser(userId)

    val mensaUrl = FeedURL.mensa + LocalDateTime.getDateInFuture(daysInFuture)
    val content = HTTPGet.cacheGet(mensaUrl)

    if (content.isDefined) {
      //parses the json entries and stores them in a MensaMoltke object
      val mensa = JsonMethods.parse(content.get).extract[MensaMoltke]
      val currentContent = cbq.message.get.text.getOrElse("")
      val newContent = mensa.toString(daysInFuture, priceConfig)

      //Since the currentContent doesn't include any html entities, such as <b>...</b> but the
      // newContent does, only the dates, which are in the second line, are compared.
      val currentContentDateLine: Array[String] = currentContent.split("\n")
      val newContentDateLine: Array[String] = newContent.split("\n")

      if (currentContentDateLine.length > 1 && newContentDateLine.length > 1 &&
          currentContentDateLine(1) == newContentDateLine(1)) {
        logger.debug("User clicked on the same date, as is displayed.")
        logger.debug("Don't update message.")
      } else {
        request(
          EditMessageText(
            Some(chatId),
            Some(messageId),
            replyMarkup = Some(createInlineKeyboardMarkup()),
            text = newContent,
            parseMode = Some(ParseMode.HTML)
          ))
      }

    }
  }

  def tagMensa: String => String = prefixTag("Mensa")

  def setDaysToAddArray(): Array[Int] = {
    val daysToAdd = Array.fill[Int](5)(0)

    //breaks on mondays
    if (LocalDateTime.getWeekDayPlusBonusDays(0) + 4 < 6) {
      daysToAdd
    } else {

      for (bonusDays <- 0 to 4) {
        val weekDay = LocalDateTime.getWeekDayPlusBonusDays(bonusDays)
        (bonusDays: @switch) match {
          case 0 =>
            (weekDay: @switch) match {
              case 6 =>
                daysToAdd(0) = 2
                daysToAdd(1) = 2
                daysToAdd(2) = 2
                daysToAdd(3) = 2
                daysToAdd(4) = 2
                return daysToAdd
              case 7 =>
                daysToAdd(0) = 1
                daysToAdd(1) = 1
                daysToAdd(2) = 1
                daysToAdd(3) = 1
                daysToAdd(4) = 1
                return daysToAdd
              case _ =>
            }
          case 1 =>
            if (weekDay == 6) {
              daysToAdd(1) = 2
              daysToAdd(2) = 2
              daysToAdd(3) = 2
              daysToAdd(4) = 2
              return daysToAdd
            }
          case 2 =>
            if (weekDay == 6) {
              daysToAdd(2) = 2
              daysToAdd(3) = 2
              daysToAdd(4) = 2
              return daysToAdd
            }
          case 3 =>
            if (weekDay == 6) {
              daysToAdd(3) = 2
              daysToAdd(4) = 2
              return daysToAdd
            }
          case 4 =>
            if (weekDay == 6) {
              daysToAdd(4) = 2
              return daysToAdd
            }
          case _ =>
        }
      }
      daysToAdd
    }
  }
}
