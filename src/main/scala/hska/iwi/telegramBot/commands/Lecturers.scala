package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.lecturers.Lecturer
import hska.iwi.telegramBot.service.{FeedURL, HTTPGet}
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.methods.{ParseMode, SendMessage}
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
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  var lecturers: Option[Seq[Lecturer]] = None

  onCommand("/profs") { implicit msg =>
    logger.debug(s"${msg.from.getOrElse("")} requested data about lecturers")
    val content = HTTPGet.get(FeedURL.lecturer)
    if (content.isDefined) {
      //parses the json entries and stores them in a MensaMoltke object
      lecturers = Some(JsonMethods.parse(content.get).extract[Seq[Lecturer]].sortBy(_.lastname))
      //reply(RoomFormatter.format(lecturers), parseMode = Some(ParseMode.HTML))

      logger.info(s"Received ${lecturers.get.size} Lecturers.")

      reply("Wähle einen Professor aus, zu dem Du genauere Informationen erhalten möchtest.",
            replyMarkup = Some(createInlineKeyboardMarkup(lecturers.get)))
    }
  }

  def createInlineKeyboardMarkup(lecturersSet: Seq[Lecturer]): InlineKeyboardMarkup = {
    val buttonSeq =
      lecturersSet
        .map(lec => InlineKeyboardButton.callbackData(lec.lastname, tagLecturer(lec.id.toString)))

    InlineKeyboardMarkup.singleColumn(buttonSeq)
  }

  onCallbackWithTag("Lecturer") { implicit cbq: CallbackQuery =>
    val lecturerID = cbq.data.get.toInt
    logger.info(s"Received lecturer with ID: $lecturerID")

    if (lecturers.isDefined) {
      val selectedLecturer = lecturers.get.find(lec => lec.id == lecturerID)
      if (selectedLecturer.isDefined) {
        ackCallback()(cbq)
        request(
          SendMessage(ChatId(cbq.message.get.chat.id),
                      s"${selectedLecturer.get.toString}",
                      parseMode = Some(ParseMode.HTML)))
      } else {
        logger.warn(s"No information received about lecturer with id: $lecturerID")
      }
    }
  }

  def tagLecturer: String => String = prefixTag("Lecturer")
}
