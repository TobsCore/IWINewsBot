package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.lecturers.Lecturer
import hska.iwi.telegramBot.service.{FeedURL, HTTPGet}
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.methods.{EditMessageText, ParseMode, SendMessage}
import info.mukel.telegrambot4s.models.{
  CallbackQuery,
  ChatId,
  InlineKeyboardButton,
  InlineKeyboardMarkup
}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

trait Lecturers extends Commands with Callbacks {
  _: TelegramBot =>
  implicit val jsonDefaultFormats: DefaultFormats.type = org.json4s.DefaultFormats

  var lecturers: Option[Seq[Lecturer]] = None

  onCommand("/profs") { implicit msg =>
    logger.info(s"${msg.from.getOrElse("")} requested lecturers")
    val content = HTTPGet.cacheGet(FeedURL.lecturer)
    if (content.isDefined) {
      //parses the json entries and stores them in a lecturers object
      lecturers = Some(JsonMethods.parse(content.get).extract[Seq[Lecturer]].sortBy(_.lastname))

      logger.debug(s"Received ${lecturers.get.size} Lecturers.")

      reply("Zu wem möchtest Du genauere Informationen erhalten?",
            replyMarkup = Some(createInlineKeyboardMarkup(lecturers.get)))
    }
  }

  def createInlineKeyboardMarkup(lecturersSet: Seq[Lecturer]): InlineKeyboardMarkup = {
    val buttonSeq =
      lecturersSet
        .map(lec => InlineKeyboardButton.callbackData(lec.lastname, tagLecturer(lec.id.toString)))

    InlineKeyboardMarkup(buttonSeq.sliding(2, 2).toSeq)
  }

  onCallbackWithTag("Lecturer") { implicit cbq: CallbackQuery =>
    // Always needs to acknowledge the callback
    ackCallback()(cbq)
    val lecturerID = cbq.data.get.toInt
    logger.debug(s"Received lecturer with ID: $lecturerID")

    val chatId = ChatId(cbq.message.get.chat.id)
    val messageId = cbq.message.get.messageId

    if (lecturers.isDefined) {
      val selectedLecturer = lecturers.get.find(lec => lec.id == lecturerID)
      if (selectedLecturer.isDefined) {
        request(
          EditMessageText(
            Some(chatId),
            Some(messageId),
            text = "Hier sind die Informationen:",
            parseMode = Some(ParseMode.HTML)
          ))

        request(
          SendMessage(chatId,
                      text = s"${selectedLecturer.get.toString}",
                      parseMode = Some(ParseMode.HTML)))
      } else {
        logger.warn(s"No information received about lecturer with id: $lecturerID")
      }
    }
  }

  def tagLecturer: String => String = prefixTag("Lecturer")
}
