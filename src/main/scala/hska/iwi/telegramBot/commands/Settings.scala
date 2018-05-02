package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.news._
import hska.iwi.telegramBot.service.{Instances, PriceConfig, Tagging, UserID}
import hska.iwi.telegramBot.study.Study
import info.mukel.telegrambot4s.api.Extractors.Int
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.methods.{EditMessageText, ParseMode}
import info.mukel.telegrambot4s.models._

trait Settings extends Commands with Callbacks with Instances {
  _: TelegramBot =>

  onCommand("/settings") { implicit msg =>
    using(_.from) { user =>
      {
        logger.info(s"User $user selected /settings")

        reply(mainSettingsText(UserID(user.id)),
              replyMarkup = Some(mainSettingMarkup()),
              parseMode = Some(ParseMode.HTML))
      }
    }
  }

  onCallbackWithTag(Tagging.SETTINGS_PREFIX) { implicit cbq: CallbackQuery =>
    ackCallback()
    if (cbq.data.isDefined) {
      val userID = UserID(cbq.from.id)
      val response: (String, Option[InlineKeyboardMarkup]) = cbq.data.get match {
        case Tagging.ABONNEMENT => (aboSettingsText(), aboSettingsMarkup(userID))
        case Tagging.PRICES =>
          (priceSettingsText(), Some(priceSettingsMarkup(userID)))
        case Tagging.STUDIUM =>
          ("Wähle Deinen Studiengang aus:", Some(studiumCourseSettingsMarkup(userID)))
        case topic =>
          logger.error(s"Not supported tag has been used in general setting. The tag was: $topic")
          ("Not supported tag", None)

      }

      requestEditMessage(cbq.message.get.chat.id,
                         cbq.message.get.messageId,
                         response._1,
                         response._2)
    }
  }

  onCallbackWithTag(Tagging.PRICE_PREFIX) { implicit cbq: CallbackQuery =>
    val buttonData = cbq.data
    val user = UserID(cbq.from.id)
    if (buttonData.isDefined) {
      logger.debug(s"$user is changing price settings. Selection: ${buttonData.get}")
      val response: (String, Option[InlineKeyboardMarkup]) = buttonData.get match {
        case Tagging.STUDENT =>
          ackCallback(Some("Preise für Studierende wurde gespeichert"))
          savePriceSettings(buttonData.get, user)
          (mainSettingsText(user), Some(mainSettingMarkup()))
        case Tagging.EMPLOYEE =>
          ackCallback(Some("Preise für Mitarbeiter/innen wurde gespeichert"))
          savePriceSettings(buttonData.get, user)
          (mainSettingsText(user), Some(mainSettingMarkup()))
        case Tagging.BOTH =>
          ackCallback(Some("Preise für Studierende und Mitarbeiter/innen wurde gespeichert"))
          savePriceSettings(buttonData.get, user)
          (mainSettingsText(user), Some(mainSettingMarkup()))
        case Tagging.BACK =>
          ackCallback()
          (mainSettingsText(user), Some(mainSettingMarkup()))
        case other =>
          ackCallback()
          logger.error(s"Not supported tag has been used in general setting. The tag was: $other")
          ("Not supported tag", None)
      }

      requestEditMessage(cbq.message.get.chat.id,
                         cbq.message.get.messageId,
                         response._1,
                         response._2)
    }
  }

  onCallbackWithTag(Tagging.STUDIUM_PREFIX) { implicit cbq: CallbackQuery =>
    val buttonData = cbq.data
    val user = UserID(cbq.from.id)
    if (buttonData.isDefined) {
      val response: (String, Option[InlineKeyboardMarkup]) = buttonData.get match {
        case Tagging.INFB =>
          ackCallback(Some("INFB ausgewählt"))
          val studyID = Study.getID(INFB)
          ("Semester auswählen:", Some(semesterSettingsMarkup(7, studyID.get)))
        case Tagging.MKIB =>
          ackCallback(Some("MKIB ausgewählt"))
          val studyID = Study.getID(MKIB)
          ("Semester auswählen:", Some(semesterSettingsMarkup(7, studyID.get)))
        case Tagging.INFM =>
          ackCallback(Some("INFM ausgewählt"))
          ("Vertiefungsrichtung auswählen:", Some(studiumSpecialisationSettingsMarkup()))
        case Tagging.BACK =>
          ackCallback()
          (mainSettingsText(user), Some(mainSettingMarkup()))
        case other =>
          ackCallback()
          logger.error(s"Not supported tag has been used in studium setting. The tag was: $other")
          ("Not supported tag", None)
      }

      requestEditMessage(cbq.message.get.chat.id,
                         cbq.message.get.messageId,
                         response._1,
                         response._2)
    }
  }

  onCallbackWithTag(Tagging.ABO_PREFIX) { implicit cbq: CallbackQuery =>
    val buttonData = cbq.data
    implicit val user: UserID = UserID(cbq.from.id)
    if (buttonData.isDefined) {
      logger.debug(s"$user is changing abo settings")
      val msg = cbq.message.get
      val response: (String, Option[InlineKeyboardMarkup]) = buttonData.get match {
        case Tagging.MKIB        => saveAboSettings(MKIB)
        case Tagging.INFB        => saveAboSettings(INFB)
        case Tagging.INFM        => saveAboSettings(INFM)
        case Tagging.FACULTYNEWS => saveAboSettings(Faculty)
        case Tagging.BACK        => (mainSettingsText(user), Some(mainSettingMarkup()))
        case _ =>
          logger.error("Unknown tag received in callback for Abo settings.")
          ("Es tut uns leid, aber es ist ein Fehler aufgetreten \uD83D\uDE48.", None)
      }

      requestEditMessage(msg.chat.id, msg.messageId, response._1, response._2)
    }
  }

  onCallbackWithTag(Tagging.SEMESTER_PREFIX) { implicit cbq: CallbackQuery =>
    val buttonData = cbq.data
    implicit val user: UserID = UserID(cbq.from.id)
    if (buttonData.isDefined) {
      logger.debug(s"$user is changing semester settings")
      val msg = cbq.message.get
      // The study information are appended and seperated by a `-`, therefore they have to be split
      val tags = buttonData.get.split('-')

      val response: (String, Option[InlineKeyboardMarkup]) = if (tags.size != 2) {
        buttonData.get match {
          case Tagging.BACK => (mainSettingsText(user), Some(mainSettingMarkup()))
          case _ =>
            logger.error(
              s"Unknown tag (${buttonData.get}) received in callback for semester settings.")
            ("Es tut uns leid, aber es ist ein Fehler aufgetreten \uD83D\uDE48.", None)
        }
      } else {
        tags(0) match {
          case Int(semester) =>
            val studyID = Integer.parseInt(tags(1))
            val studyData = Study.infoByID(studyID)
            val study = Study(studyData.get._1, studyData.get._2, semester)

            ackCallback(Some(s"Speichere Studiendaten: $study"))
            logger.info(s"Saving $user study data: $study")
            redis.setStudySettingsForUser(user, study)
            (mainSettingsText(user), Some(mainSettingMarkup()))
          case _ =>
            logger.error(
              s"Unknown tag (${buttonData.get}) received in callback for semester settings.")
            ("Es tut uns leid, aber es ist ein Fehler aufgetreten \uD83D\uDE48.", None)
        }
      }

      requestEditMessage(msg.chat.id, msg.messageId, response._1, response._2)
    }
  }
  onCallbackWithTag(Tagging.SPECIALILISATION_PREFIX) { implicit cbq: CallbackQuery =>
    // TODO: Implement this correctly. This is crap!
    val buttonData = cbq.data
    implicit val user: UserID = UserID(cbq.from.id)
    if (buttonData.isDefined) {
      logger.debug(s"$user is changing semester settings")
      val msg = cbq.message.get
      val response: (String, Option[InlineKeyboardMarkup]) = buttonData.get match {
        case Tagging.INTERACTIVE_SYSTEMS =>
          ackCallback(Some("Interaktive Systeme ausgewählt"))
          val studyID = Study.getID(INFM, Some(InteractiveSystems))
          ("Semester auswählen:", Some(semesterSettingsMarkup(3, studyID.get)))
        case Tagging.SOFTWARE_ENGINEERING =>
          ackCallback(Some("Software-Engineering ausgewählt"))
          val studyID = Study.getID(INFM, Some(SoftwareEngineering))
          ("Semester auswählen:", Some(semesterSettingsMarkup(3, studyID.get)))
        case Tagging.BACK => (mainSettingsText(user), Some(mainSettingMarkup()))
        case _ =>
          logger.error("Unknown tag received in callback for Abo settings.")
          ("Es tut uns leid, aber es ist ein Fehler aufgetreten \uD83D\uDE48.", None)
      }

      requestEditMessage(msg.chat.id, msg.messageId, response._1, response._2)
    }
  }

  def mainSettingMarkup(): InlineKeyboardMarkup = {
    val buttons = createButtons(Seq(("Abonnement", Tagging.ABONNEMENT),
                                    ("Preise", Tagging.PRICES),
                                    ("Studium", Tagging.STUDIUM)),
                                Tagging.SETTINGS_PREFIX)
    InlineKeyboardMarkup.singleColumn(buttons)
  }

  def aboSettingsMarkup(user: UserID): Option[InlineKeyboardMarkup] = {
    val configOption = redis.getConfigFor(user)
    val facultyNewsValueOption = redis.getFacultyConfigForUser(user)

    if (configOption.isEmpty || facultyNewsValueOption.isEmpty) {
      // Set the default values for configuration
      redis.setDefaultUserConfig(user)
    }
    // Get the previously received configuration or get the newly set default options
    val config = configOption.getOrElse(redis.getConfigFor(user).get)
    val facultyNewsValue = facultyNewsValueOption.getOrElse(redis.getFacultyConfigForUser(user).get)

    Some(
      InlineKeyboardMarkup.singleColumn(
        Seq(
          InlineKeyboardButton.callbackData(aboButtonTexts(!config(INFB), INFB),
                                            prefixTag(Tagging.ABO_PREFIX)(Tagging.INFB)),
          InlineKeyboardButton.callbackData(aboButtonTexts(!config(MKIB), MKIB),
                                            prefixTag(Tagging.ABO_PREFIX)(Tagging.MKIB)),
          InlineKeyboardButton.callbackData(aboButtonTexts(!config(INFM), INFM),
                                            prefixTag(Tagging.ABO_PREFIX)(Tagging.INFM)),
          InlineKeyboardButton.callbackData(buttonText4Faculty(!facultyNewsValue),
                                            prefixTag(Tagging.ABO_PREFIX)(Tagging.FACULTYNEWS)),
          InlineKeyboardButton.callbackData("« Zurück zu Settings",
                                            prefixTag(Tagging.ABO_PREFIX)("Back"))
        )
      ))
  }

  def priceSettingsMarkup(userID: UserID): InlineKeyboardMarkup = {
    val buttons =
      createButtons(
        Seq(
          ("Studierende", Tagging.STUDENT),
          ("Mitarbeiter/innen", Tagging.EMPLOYEE),
          ("Studierende und Mitarbeiter/innen", Tagging.BOTH),
          ("« Zurück zu Settings", Tagging.BACK)
        ),
        Tagging.PRICE_PREFIX
      )
    InlineKeyboardMarkup.singleColumn(buttons)
  }

  def semesterSettingsMarkup(semesterAmount: Int, selectedStudyID: Int): InlineKeyboardMarkup = {
    val buttons =
      createButtons(
        (1 to semesterAmount)
          .map(e => (s"${e.toString}. Semester", s"${e.toString}-$selectedStudyID")) :+ ("« Zurück zu Settings", Tagging.BACK),
        Tagging.SEMESTER_PREFIX
      )
    InlineKeyboardMarkup(buttons.sliding(2, 2).toSeq)
  }

  def studiumCourseSettingsMarkup(userID: UserID): InlineKeyboardMarkup = {
    val buttons =
      createButtons(Seq(("INFB", Tagging.INFB),
                        ("MKIB", Tagging.MKIB),
                        ("INFM", Tagging.INFM),
                        ("« Zurück zu Settings", Tagging.BACK)),
                    Tagging.STUDIUM_PREFIX)
    InlineKeyboardMarkup.singleColumn(buttons)
  }

  def studiumSpecialisationSettingsMarkup(): InlineKeyboardMarkup = {
    // Tuple consists of the printed name and the Tag name for each button
    val buttons =
      createButtons(
        Seq(
          ("Software-Engineering", Tagging.SOFTWARE_ENGINEERING),
          ("Interaktive Systeme", Tagging.INTERACTIVE_SYSTEMS),
          ("« Zurück zu Settings", Tagging.BACK)
        ),
        Tagging.SPECIALILISATION_PREFIX
      )
    InlineKeyboardMarkup.singleColumn(buttons)
  }

  def createButtons(seq: Seq[(String, String)], tagPrefix: String): Seq[InlineKeyboardButton] = {
    seq.map(entry =>
      InlineKeyboardButton.callbackData(text = entry._1, prefixTag(tagPrefix)(entry._2)))
  }

  private def saveAboSettings(course: SubscribableMember)(
      implicit user: UserID,
      cbq: CallbackQuery): (String, Option[InlineKeyboardMarkup]) = {

    course match {
      case course: Course =>
        val setValue = !redis.getConfigFor(user).getOrElse(Map()).find(_._1 == course).head._2
        redis.setUserConfig(user, course, setValue)
        ackCallback(Some(notificationText(setValue, course)))
      case _: Faculty.type =>
        val setValue = !redis.getFacultyConfigForUser(user).getOrElse(false)
        redis.setFacultyConfigForUser(user, setValue)
        ackCallback(Some(notificationText4Faculty(setValue)))
      case _ =>
        logger.error(s"Type ${course.getClass} is not allowed")
        ackCallback()
    }
    (aboSettingsText(), aboSettingsMarkup(user))
  }

  private def savePriceSettings(settings: String, userID: UserID): Unit = {
    val priceConfig = PriceConfig(settings.toLowerCase())
    redis.setPriceConfigForUser(priceConfig, userID)
  }

  private def aboSettingsText(): String =
    s"Hier kannst Du festlegen, zu welchen Studiengängen (INFB, MKIB und INFM) Du Nachrichten des" +
      s" <i>Schwarzen Bretts</i> erhalten möchtest. Außerdem kannst Du Nachrichten der " +
      s"IWI-Fakultät abonnieren."

  private def priceSettingsText(): String =
    "Aus welcher Sicht möchtest Du die Mensapreise anzeigen lassen?"

  private def mainSettingsText(userID: UserID): String = {
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

  private def aboButtonTexts(value: Boolean, course: SubscribableMember): String =
    if (value) {
      s"$course abonnieren"
    } else {
      s"$course abbestellen"
    }

  private def buttonText4Faculty(value: Boolean): String =
    if (value) {
      s"IWI-Fakultät abonnieren"
    } else {
      s"IWI-Fakultät abbestellen"
    }

  private def notificationText4Faculty(selection: Boolean): String =
    if (selection) {
      s"Nachrichten der Fakultät sind abonniert"
    } else {
      s"Nachrichten der Fakultät sind abbestellt"
    }

  private def notificationText(selection: Boolean, course: Course): String =
    if (selection) {
      s"Nachrichten für $course sind abonniert"
    } else {
      s"Nachrichten für $course sind abbestellt"
    }

  private def requestEditMessage(chatID: ChatId,
                                 messageID: Int,
                                 text: String,
                                 markup: Option[InlineKeyboardMarkup]): Unit = {
    request(
      EditMessageText(
        Some(chatID),
        Some(messageID),
        replyMarkup = markup,
        text = text,
        parseMode = Some(ParseMode.HTML)
      ))
  }

}
