package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.news._
import hska.iwi.telegramBot.service._
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.methods.{EditMessageText, ParseMode}
import info.mukel.telegrambot4s.models._

trait Settings extends Commands with Callbacks with Instances {
  _: TelegramBot =>
  val settingsTag = "Settings"
  val priceTag = "PriceConfig"
  val aboTag = "AboConfig"

  val MKIBSelectionTAG = "MKIB"
  val INFBSelectionTAG = "INFB"
  val INFMSelectionTAG = "INFM"
  val FacultyNewsSelectionTAG = "Faculty"

  onCommand("/settings") { implicit msg =>
    using(_.from) { user =>
      {
        logger.info(s"User $user selected /settings")

        reply(settingsGreeting(UserID(user.id)),
              replyMarkup = Some(mainSettingsInlineKeyboardMarkup()),
              parseMode = Some(ParseMode.HTML))
      }
    }
  }

  def mainSettingsInlineKeyboardMarkup(): InlineKeyboardMarkup = {
    val buttons = Seq("Abonnement", "Preise", "Studium").map(name =>
      InlineKeyboardButton.callbackData(text = name, prefixTag(settingsTag)(name)))
    InlineKeyboardMarkup.singleColumn(buttons)
  }

  def aboSettingsInlineKeyboardMarkup(user: UserID): Option[InlineKeyboardMarkup] = {

    val configOption = redis.getConfigFor(user)
    val facultyNewsValueOption = redis.getFacultyConfigForUser(user)
    if (configOption.isDefined && facultyNewsValueOption.isDefined) {
      val config = configOption.get
      val facultyNewsValue = facultyNewsValueOption.get

      Some(
        InlineKeyboardMarkup.singleColumn(
          Seq(
            InlineKeyboardButton.callbackData(buttonText(!config(INFB), INFB),
                                              prefixTag(aboTag)(INFBSelectionTAG)),
            InlineKeyboardButton.callbackData(buttonText(!config(MKIB), MKIB),
                                              prefixTag(aboTag)(MKIBSelectionTAG)),
            InlineKeyboardButton.callbackData(buttonText(!config(INFM), INFM),
                                              prefixTag(aboTag)(INFMSelectionTAG)),
            InlineKeyboardButton.callbackData(buttonText4Faculty(!facultyNewsValue),
                                              prefixTag(aboTag)(FacultyNewsSelectionTAG)),
            InlineKeyboardButton.callbackData("« Zurück zu Settings", prefixTag(aboTag)("Back"))
          )
        ))
    } else {
      None
    }
  }

  def priceSettingsInlineKeyboardMarkup(userID: UserID): InlineKeyboardMarkup = {
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
      val userID = UserID(cbq.from.id)
      val response: (String, Option[InlineKeyboardMarkup]) = cbq.data.get match {
        case "Abonnement" => (aboSettingsText(), aboSettingsInlineKeyboardMarkup(userID))
        case "Preise" =>
          (priceSettingText(), Some(priceSettingsInlineKeyboardMarkup(userID)))
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
          (settingsGreeting(user), Some(mainSettingsInlineKeyboardMarkup()))
        case "Employee" =>
          ackCallback(Some("Preise für Mitarbeiter/innen wurde gespeichert"))
          savePriceSettings(buttonData.get, user)
          (settingsGreeting(user), Some(mainSettingsInlineKeyboardMarkup()))
        case "Both" =>
          ackCallback(Some("Preise für Studierende und Mitarbeiter/innen wurde gespeichert"))
          savePriceSettings(buttonData.get, user)
          (settingsGreeting(user), Some(mainSettingsInlineKeyboardMarkup()))
        case "Back" =>
          ackCallback()
          (settingsGreeting(user), Some(mainSettingsInlineKeyboardMarkup()))
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

  onCallbackWithTag(aboTag) { implicit cbq: CallbackQuery =>
    val buttonData = cbq.data
    val user = UserID(cbq.from.id)
    if (buttonData.isDefined) {
      val response: (String, Option[InlineKeyboardMarkup]) = buttonData.get match {
        case MKIBSelectionTAG        => callbackMethod(MKIB)
        case INFBSelectionTAG        => callbackMethod(INFB)
        case INFMSelectionTAG        => callbackMethod(INFM)
        case FacultyNewsSelectionTAG => callbackMethod(Faculty)
        case "Back" =>
          (settingsGreeting(user), Some(mainSettingsInlineKeyboardMarkup()))

        case _ => throw new IllegalArgumentException
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

  private def callbackMethod(course: SubscribableMember)(
      implicit cbq: CallbackQuery): (String, Option[InlineKeyboardMarkup]) = {
    if (cbq.message.isEmpty) {
      logger.error(
        "There was no content to the callback method. This shouldn't happen, as the user cannot " +
          "set the subscriptions correctly. If this error occurs, check this.")
      aboSettingsCallback(cbq)
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
      aboSettingsCallback(cbq)
    }
  }

  private def aboSettingsCallback(cbq: CallbackQuery): (String, Option[InlineKeyboardMarkup]) = {
    if (cbq.message.isDefined) {
      val user = UserID(cbq.message.get.chat.id.toInt)
      (aboSettingsText(), aboSettingsInlineKeyboardMarkup(user))
    } else {
      logger.error(
        "No message has been defined for selection of abo settings. This shouldn't happen.")
      ("Ein Fehler ist aufgetreten", None)
    }
  }

  private def savePriceSettings(settings: String, userID: UserID): Unit = {
    val priceConfig = PriceConfig(settings.toLowerCase())
    redis.setPriceConfigForUser(priceConfig, userID)
  }

  private def aboSettingsText(): String =
    s"Hier kannst Du festlegen, zu welchen Studiengängen (INFB, MKIB und INFM) Du Nachrichten des" +
      s" <i>Schwarzen Bretts</i> erhalten möchtest. Außerdem kannst Du Nachrichten der " +
      s"IWI-Fakultät abonnieren."

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

  private def buttonText(value: Boolean, course: SubscribableMember): String =
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
