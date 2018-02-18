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

  onCommand("/mensa") { implicit msg =>
    logger.debug(s"${msg.from.getOrElse("")} requested mensa data")
    logger.debug("received command 'mensa'")
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
    val mensaDayID = cbq.data.get.toInt
    val daysToAdd = setDaysToAddArray()
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

  def setDaysToAddArray(): Array[Int] = {
    var daysToAdd = new Array[Int](5)
    for (bonusDays <- 0 to 4) {
      val weekDay = LocalDateTime.getWeekDayPlusBonusDays(bonusDays)
      bonusDays match {
        case 0 => {
          weekDay match {
            case 6 => {
              daysToAdd(0) = 2
              daysToAdd(1) = 2
              daysToAdd(2) = 2
              daysToAdd(3) = 2
              daysToAdd(4) = 2
              return daysToAdd
            }
            case 7 => {
              daysToAdd(0) = 1
              daysToAdd(1) = 1
              daysToAdd(2) = 1
              daysToAdd(3) = 1
              daysToAdd(4) = 1
              return daysToAdd
            }
            case _ =>
          }
        }
        case 1 => {
          weekDay match {
            case 6 => {
              daysToAdd(0) = 0
              daysToAdd(1) = 2
              daysToAdd(2) = 2
              daysToAdd(3) = 2
              daysToAdd(4) = 2
              return daysToAdd
            }
            case _ =>
          }
        }
        case 2 => {
          weekDay match {
            case 6 => {
              daysToAdd(0) = 0
              daysToAdd(1) = 0
              daysToAdd(2) = 2
              daysToAdd(3) = 2
              daysToAdd(4) = 2
              return daysToAdd
            }
            case _ =>
          }
        }
        case 3 => {
          weekDay match {
            case 6 => {
              daysToAdd(0) = 0
              daysToAdd(1) = 0
              daysToAdd(2) = 0
              daysToAdd(3) = 2
              daysToAdd(4) = 2
              return daysToAdd
            }
            case _ =>
          }
        }
        case 4 => {
          weekDay match {
            case 6 => {
              daysToAdd(0) = 0
              daysToAdd(1) = 0
              daysToAdd(2) = 0
              daysToAdd(3) = 0
              daysToAdd(4) = 2
              return daysToAdd
            }
            case _ =>
          }
        }
        case _ =>
      }
    }
    return daysToAdd
  }
}
