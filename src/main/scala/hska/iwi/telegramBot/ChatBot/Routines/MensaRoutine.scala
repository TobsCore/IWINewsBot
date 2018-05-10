package hska.iwi.telegramBot.ChatBot.Routines

import com.rivescript.RiveScript
import com.rivescript.`macro`.Subroutine
import hska.iwi.telegramBot.mensa.MensaMoltke
import hska.iwi.telegramBot.service._
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.models.{Chat, ChatType, Message}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

class MensaRoutine extends Subroutine with Instances {

  implicit val jsonDefaultFormats: DefaultFormats.type = org.json4s.DefaultFormats

  override def call(rs: RiveScript, args: Array[String]): String = args.headOption match {
    case Some(param) => callMensa(param, rs)
    case _ =>
      val stringBuilder = new StringBuilder
      stringBuilder.append("Heute gibt es:\n")
      stringBuilder.append(mensaRequest(rs, 0).getOrElse("Keine Daten vorhanden."))
      stringBuilder.toString()
  }

  private def callMensa(param: String, rs: RiveScript): String = {
    val stringBuilder = new StringBuilder
    param.toLowerCase() match {
      case "heute" =>
        stringBuilder.append("Heute gibt es:\n")
        stringBuilder.append(mensaRequest(rs, 0).getOrElse("Keine Daten vorhanden."))
        stringBuilder.toString()
      case "morgen" =>
        stringBuilder.append("Morgen gibt es:\n")
        stringBuilder.append(mensaRequest(rs, 1).getOrElse("Keine Daten vorhanden."))
        stringBuilder.toString()
      case "übermorgen" =>
        stringBuilder.append("Übermorgen gibt es:\n")
        stringBuilder.append(mensaRequest(rs, 2).getOrElse("Keine Daten vorhanden."))
        stringBuilder.toString()
      case "gestern" =>
        stringBuilder.append("Gestern gab es:\n")
        stringBuilder.append(mensaRequest(rs, -1).getOrElse("Keine Daten vorhanden."))
        stringBuilder.toString()
      case "vorgestern" =>
        stringBuilder.append("Vorgestern gab es:\n")
        stringBuilder.append(mensaRequest(rs, -2).getOrElse("Keine Daten vorhanden."))
        stringBuilder.toString()
      case _ =>
        "Das habe ich leider nicht verstanden. Möchtest du wissen, was es heute, morgen oder übermorgen in der Mensa gibt? Für weitere Tage rufe /mensa auf."
    }
  }

  private def mensaRequest(rs: RiveScript, daysInFuture: Int): Option[String] = {
    val mensaUrl = FeedURL.mensa + LocalDateTime.getDateInFuture(daysInFuture)
    val content = HTTPGet.get(mensaUrl)

    if (content.isDefined) {
      //parses the json entries and stores them in a MensaMoltke object
      val mensa = JsonMethods.parse(content.get).extract[MensaMoltke]
      val userID = UserID(rs.currentUser().toInt)
      val priceConfig = redis.getPriceConfigForUser(userID)
      Some(mensa.toString(daysInFuture, priceConfig))
    } else {
      None
    }
  }

}
