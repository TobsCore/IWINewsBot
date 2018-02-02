package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.news.{Course, _}
import hska.iwi.telegramBot.service.{Instances, UserID}
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.methods.{EditMessageReplyMarkup, ParseMode}
import info.mukel.telegrambot4s.models.{
  CallbackQuery,
  ChatId,
  InlineKeyboardButton,
  InlineKeyboardMarkup
}

trait AboSettings extends Commands with Callbacks with Instances {
  _: TelegramBot =>

  val MKIBSelectionTAG = "MKIBSELECTION"
  val INFBSelectionTAG = "INFBSELECTION"
  val INFMSelectionTAG = "INFMSELECTION"
  val FacultyNewsSelectionTAG = "FacultySELECTION"

  def tagAbo: String => String = prefixTag("Abo")

  onCommand("/abo") { implicit msg =>
    {
      using(_.from) { user =>
        if (redis.isMember(UserID(user.id))) {
          logger.debug(s"User $user is changing settings.")
          val userId = UserID(msg.chat.id.toInt)
          val config = redis.getConfigFor(userId)
          val facultyNewsConfigValue = redis.getFacultyConfigForUser(userId)
          if (config.isDefined) {
            reply(
              s"Hier kannst Du festlegen, zu welchen Studiengängen (INFB, MKIB und INFM) Du Nachrichten des <i>Schwarzen Bretts</i> erhalten möchtest. Außerdem kannst Du Nachrichten der IWI-Fakultät abonnieren.",
              replyMarkup = Some(createInlineKeyboardMarkup(config.get, facultyNewsConfigValue.get)),
              parseMode = Some(ParseMode.HTML)
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

  onCallbackWithTag("Abo") { implicit cbq: CallbackQuery =>
    val tag = cbq.data
    if (tag.isDefined) {
      tag.get match {
        case MKIBSelectionTAG        => callbackMethod(MKIB)
        case INFBSelectionTAG        => callbackMethod(INFB)
        case INFMSelectionTAG        => callbackMethod(INFM)
        case FacultyNewsSelectionTAG => callbackMethod(Faculty)
        case _                       => throw new IllegalArgumentException
      }
    }
  }

  def callbackMethod(course: SubscribableMember)(implicit cbq: CallbackQuery): Unit = {
    if (cbq.message.isEmpty) {
      logger.error(
        "There was no content to the callback method. This shouldn't happen, as the user cannot " +
          "set the subscriptions correctly. If this error occurs, check this.")
    } else {

      val userID: UserID = UserID(cbq.message.get.chat.id.toInt)
      course match {
        case course: Course =>
          val setValue = !redis.getConfigFor(userID).getOrElse(Map()).find(_._1 == course).head._2
          redis.setUserConfig(userID, course, setValue)
          // Notification only shown to the user who pressed the button.
          ackCallback(Some(notificationText(setValue, course)))
        case _: Faculty.type =>
          val setValue = !redis.getFacultyConfigForUser(userID).getOrElse(false)
          redis.setFacultyConfigForUser(userID, setValue)
          // Notification only shown to the user who pressed the button.
          ackCallback(Some(notificationText4Faculty(setValue)))
        case _ => throw new IllegalArgumentException(s"Type ${course.getClass} is not allowed")
      }
      callback(cbq)
    }
  }

  private def callback(cbq: CallbackQuery): Unit =
    cbq.message.foreach(msg => {
      val userId = UserID(msg.chat.id.toInt)
      val config = redis.getConfigFor(userId)
      val facultyNewsConfigValue = redis.getFacultyConfigForUser(userId)
      if (config.isDefined) {
        request(
          EditMessageReplyMarkup(
            chatId = Some(ChatId(msg.source)),
            messageId = Some(msg.messageId),
            replyMarkup = Some(createInlineKeyboardMarkup(config.get, facultyNewsConfigValue.get))))
      } else {
        logger.error(s"Could not get configuration for user ${msg.chat.id}")
      }
    })

  def createInlineKeyboardMarkup(config: Map[Course, Boolean],
                                 facultyNewsValue: Boolean): InlineKeyboardMarkup = {

    InlineKeyboardMarkup.singleColumn(
      Seq(
        InlineKeyboardButton.callbackData(buttonText(!config(INFB), INFB),
                                          tagAbo(INFBSelectionTAG)),
        InlineKeyboardButton.callbackData(buttonText(!config(MKIB), MKIB),
                                          tagAbo(MKIBSelectionTAG)),
        InlineKeyboardButton.callbackData(buttonText(!config(INFM), INFM),
                                          tagAbo(INFMSelectionTAG)),
        InlineKeyboardButton.callbackData(buttonText4Faculty(!facultyNewsValue),
                                          tagAbo(FacultyNewsSelectionTAG))
      )
    )
  }

  def buttonText(value: Boolean, course: SubscribableMember): String =
    if (value) {
      s"$course abonnieren"
    } else {
      s"$course abbestellen"
    }

  def buttonText4Faculty(value: Boolean): String =
    if (value) {
      s"IWI-Fakultät abonnieren"
    } else {
      s"IWI-Fakultät abbestellen"
    }

  def notificationText4Faculty(selection: Boolean): String =
    if (selection) {
      s"Nachrichten der Fakultät sind abonniert"
    } else {
      s"Nachrichten der Fakultät sind abbestellt"
    }

  def notificationText(selection: Boolean, course: Course): String =
    if (selection) {
      s"Nachrichten für $course sind abonniert"
    } else {
      s"Nachrichten für $course sind abbestellt"
    }

}
