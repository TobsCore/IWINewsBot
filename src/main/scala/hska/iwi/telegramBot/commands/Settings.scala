package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.service._
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.methods.{EditMessageText, ParseMode}
import info.mukel.telegrambot4s.models._

trait Settings extends Commands with Callbacks with Instances {
  _: TelegramBot =>
  val settingsTag = "Settings"
  val priceTag = "PriceConfig"

  onCommand("/settings") { implicit msg =>
    using(_.from) { user =>
      {
        logger.info(s"User $user selected /settings")

        reply(
          settingsGreeting,
          replyMarkup = Some(createSettingsInlineKeyboardMarkup()),
          parseMode = Some(ParseMode.HTML)
      }
    }
  }

  def createSettingsInlineKeyboardMarkup(): InlineKeyboardMarkup = {
    val buttons = Seq("Abonnement", "Preise", "Studium").map(name =>
      InlineKeyboardButton.callbackData(text = name, prefixTag(settingsTag)(name)))
    InlineKeyboardMarkup.singleColumn(buttons)
  }

  def priceSettingInlineKeyBoardMarkup(userID: UserID): InlineKeyboardMarkup = {
    val priceConfig = redis.getPriceConfigForUser(userID)
    val config =
      InlineKeyboardButton.callbackData(priceConfig.toString, prefixTag(priceTag)("0"))

    val priceConfigButton = Seq[InlineKeyboardButton](config)
    InlineKeyboardMarkup.singleColumn(priceConfigButton)
  }

  onCallbackWithTag(settingsTag) { implicit cbq: CallbackQuery =>
    ackCallback()
    if (cbq.data.isDefined) {
      val response: (String, Option[InlineKeyboardMarkup]) = cbq.data.get match {
        case "Abonnement" =>
          (priceSettingText(), Some(priceSettingInlineKeyBoardMarkup(UserID(cbq.from.id))))
        case "Preise"  => ("Not supported", None)
        case "Studium" => ("Not supported", None)
        case topic =>
          logger.error(s"Not supported tag has been used in general setting. The tag was: ${topic}")
          ("Not supported tag", None)

      }

      request(
        EditMessageText(
          Some(cbq.message.get.chat.id),
          Some(cbq.message.get.messageId),
          replyMarkup = response._2,
          text = response._1,
          parseMode = Some(ParseMode.HTML)
        ))
    }
  }

  onCallbackWithTag(priceTag) { implicit cbq: CallbackQuery =>
    val messageId = cbq.message.get.messageId
    val chatId = cbq.message.get.chat.id
    val userId = UserID(cbq.from.id)
    val priceConfig = redis.getPriceConfigForUser(userId)
    val newConfig = priceConfig.configValue match {
      case "student"  => "employee"
      case "employee" => "both"
      case _          => "student"
    }
    redis.setPriceConfigForUser(PriceConfig(newConfig), userId)

    val text = s"Preise für ${PriceConfig(newConfig).toString} sind ausgewählt"
    ackCallback(Some(text))

    request(
      EditMessageText(
        Some(chatId),
        Some(messageId),
        replyMarkup = Some(priceSettingInlineKeyBoardMarkup(userId)),
        text = priceSettingText(),
        parseMode = Some(ParseMode.HTML)
      ))
  }
  private def priceSettingText(): String =
    "Aus welcher Sicht möchtest Du die Mensapreise anzeigen lassen?"

  private def settingsGreeting: String = {
    // TODO: Fix this method to show actual information
    s"""<b>Einstellungen</b>
       |
       |Abonniert: MKIB, INFB, IWI
       |Preise: Studierende
       |Studium: INFM (SoftwareEngineering), 2. Semester
       |
       |Du kannst die folgenden Dinge anpassen:
     """.stripMargin
  }

}
