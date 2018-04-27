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

        reply(settingsGreeting(UserID(user.id)),
              replyMarkup = Some(createSettingsInlineKeyboardMarkup()),
              parseMode = Some(ParseMode.HTML))
      }
    }
  }

  def createSettingsInlineKeyboardMarkup(): InlineKeyboardMarkup = {
    val buttons = Seq("Abonnement", "Preise", "Studium").map(name =>
      InlineKeyboardButton.callbackData(text = name, prefixTag(settingsTag)(name)))
    InlineKeyboardMarkup.singleColumn(buttons)
  }

  def priceSettingInlineKeyBoardMarkup(userID: UserID): InlineKeyboardMarkup = {
    // Tuple consists of the printed name and the Tag name for each button
    val buttons =
      Seq(("Studierende", "Student"),
          ("Mitarbeiter/innen", "Employee"),
          ("Studierende und Mitarbeiter/innen", "Both"),
          ("« Zurück zu Settings", "Back")).map(entry =>
        InlineKeyboardButton.callbackData(text = entry._1, prefixTag(priceTag)(entry._2)))
    InlineKeyboardMarkup.singleColumn(buttons)
  }

  onCallbackWithTag(settingsTag) { implicit cbq: CallbackQuery =>
    ackCallback()
    if (cbq.data.isDefined) {
      val response: (String, Option[InlineKeyboardMarkup]) = cbq.data.get match {
        case "Abonnement" => ("Not supported", None)
        case "Preise" =>
          (priceSettingText(), Some(priceSettingInlineKeyBoardMarkup(UserID(cbq.from.id))))
        case "Studium" => ("Not supported", None)
        case topic =>
          logger.error(s"Not supported tag has been used in general setting. The tag was: $topic")
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
    val buttonData = cbq.data
    val user = UserID(cbq.from.id)
    if (buttonData.isDefined) {
      val response: (String, Option[InlineKeyboardMarkup]) = buttonData.get match {
        case "Student" =>
          ackCallback(Some("Preise für Studierende wurde gespeichert"))
          savePriceSettings(buttonData.get, user)
          (settingsGreeting(user), Some(createSettingsInlineKeyboardMarkup()))
        case "Employee" =>
          ackCallback(Some("Preise für Mitarbeiter/innen wurde gespeichert"))
          savePriceSettings(buttonData.get, user)
          (settingsGreeting(user), Some(createSettingsInlineKeyboardMarkup()))
        case "Both" =>
          ackCallback(Some("Preise für Studierende und Mitarbeiter/innen wurde gespeichert"))
          savePriceSettings(buttonData.get, user)
          (settingsGreeting(user), Some(createSettingsInlineKeyboardMarkup()))
        case "Back" =>
          ackCallback()
          (settingsGreeting(user), Some(createSettingsInlineKeyboardMarkup()))
        case other =>
          ackCallback()
          logger.error(s"Not supported tag has been used in general setting. The tag was: $other")
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

  private def savePriceSettings(settings: String, userID: UserID): Unit = {
    val priceConfig = PriceConfig(settings.toLowerCase())
    redis.setPriceConfigForUser(priceConfig, userID)
  }

  private def priceSettingText(): String =
    "Aus welcher Sicht möchtest Du die Mensapreise anzeigen lassen?"

  private def settingsGreeting(userID: UserID): String = {
    // Subscription information
    val courseNewsInfo = redis.getConfigFor(userID) match {
      case Some(aboMap) => aboMap.filter(_._2).keys.mkString(", ")
      case None =>
        logger.error(s"Could not retrieve config options for with id $userID")
        ""
    }
    val facultyNewsSubscription = redis.getFacultyConfigForUser(userID)
    val facultyInfo = if (facultyNewsSubscription.isDefined && facultyNewsSubscription.get) {
      val seperator = if (!courseNewsInfo.isEmpty) ", " else ""
      seperator + "IWI"
    } else ""

    val aboInfo =
      if (courseNewsInfo.isEmpty && facultyInfo.isEmpty) "Keine" else courseNewsInfo + facultyInfo

    // Price information
    val priceSetting = redis.getPriceConfigForUser(userID).toString

    s"""<b>Einstellungen</b>
       |
       |Abonniert: $aboInfo
       |Preise: $priceSetting
       |
       |Du kannst die folgenden Dinge anpassen:
     """.stripMargin
  }

}
