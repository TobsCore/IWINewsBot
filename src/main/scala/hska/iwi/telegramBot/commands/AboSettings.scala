package hska.iwi.telegramBot.commands

import java.lang.IllegalArgumentException

import hska.iwi.telegramBot.news
import hska.iwi.telegramBot.news.Course
import hska.iwi.telegramBot.news.Course.Course
import hska.iwi.telegramBot.service.{Instances, UserID}
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.methods.EditMessageReplyMarkup
import info.mukel.telegrambot4s.models.{
  CallbackQuery,
  ChatId,
  InlineKeyboardButton,
  InlineKeyboardMarkup
}

import scala.util.Try

trait AboSettings extends Commands with Callbacks with Instances {
  _: TelegramBot =>

  val mkibSelectionTAG = "MKIBSELECTION"
  val infbSelectionTAG = "INFBSELECTION"
  val infmSelectionTAG = "INFMSELECTION"

  private def mkibtag = prefixTag(mkibSelectionTAG) _

  private def infbtag = prefixTag(infbSelectionTAG) _

  private def infmtag = prefixTag(infmSelectionTAG) _

  onCommand("/abo") { implicit msg =>
    {
      using(_.from) { user =>
        if (redis.isMember(UserID(user.id))) {
          logger.debug(s"User $user is changing settings.")
          val config = redis.getConfigFor(UserID(user.id))
          if (config.isDefined) {
            reply(
              "Hier kannst Du festlegen, zu welchen Studiengängen Du Nachrichten erhalten möchtest.",
              replyMarkup = Some(createInlineKeyboardMarkup(config.get))
            )
          } else {
            logger.warn("Configuration for user cannot be received. He probably unsubscribed")
            reply(
              "Kann die Konfiguration nicht speichern, da der Service gestoppt wurde. Starte " +
                "den Service über /start")
          }
        } else {
          reply(
            "Der Befehl kann nicht ausgeführt werden, da der Service gestoppt wurde. Starte den " +
              "Service durch /start.")
        }
      }
    }
  }

  onCallbackWithTag(infmSelectionTAG) { implicit cbq: CallbackQuery =>
    callbackMethod(Course.INFM)
  }

  onCallbackWithTag(infbSelectionTAG) { implicit cbq: CallbackQuery =>
    callbackMethod(Course.INFB)
  }

  onCallbackWithTag(mkibSelectionTAG) { implicit cbq: CallbackQuery =>
    callbackMethod(Course.MKIB)
  }

  def callbackMethod(course: Course)(implicit cbq: CallbackQuery): Unit = {
    val setValue = getValueFromCallback(cbq)
    logger.info(s"Setting $course to $setValue")
    if (cbq.message.isEmpty) {
      logger.warn("Keine Nachricht für Callback verfügbar. Das sollte nicht passieren.")
    } else {
      redis.setUserConfig(UserID(cbq.message.get.chat.id.toInt), course, setValue)
      // Notification only shown to the user who pressed the button.
      ackCallback(Some(notificationText(setValue, course)))
      callback(cbq)
    }
  }

  private def getValueFromCallback(cbq: CallbackQuery): Boolean = {
    Try {
      cbq.data.getOrElse("false").toBoolean
    }.getOrElse(false)
  }

  private def callback(cbq: CallbackQuery): Unit =
    cbq.message.foreach(msg => {
      val config = redis.getConfigFor(UserID(msg.chat.id.toInt))
      if (config.isDefined) {
        request(
          EditMessageReplyMarkup(chatId = Some(ChatId(msg.source)),
                                 messageId = Some(msg.messageId),
                                 replyMarkup = Some(createInlineKeyboardMarkup(config.get))))
      } else {
        logger.error(s"Could not get configuration for user ${msg.chat.id}")
      }
    })

  def createInlineKeyboardMarkup(config: Map[Course, Boolean]): InlineKeyboardMarkup = {

    InlineKeyboardMarkup.singleColumn(
      Seq(
        InlineKeyboardButton.callbackData(buttonText(!config(Course.INFB), Course.INFB),
                                          infbtag((!config(Course.INFB)).toString)),
        InlineKeyboardButton.callbackData(buttonText(!config(Course.MKIB), Course.MKIB),
                                          mkibtag((!config(Course.MKIB)).toString)),
        InlineKeyboardButton.callbackData(buttonText(!config(Course.INFM), Course.INFM),
                                          infmtag((!config(Course.INFM)).toString)),
      )
    )
  }

  def buttonText(value: Boolean, course: Course): String =
    if (value) {
      s"$course abonnieren"
    } else {
      s"$course abbstellen"
    }

  def notificationText(selection: Boolean, course: Course): String =
    if (selection) {
      s"Nachrichten für $course sind abonniert"
    } else {
      s"Nachrichten für $course sind abbestellt"
    }

}
