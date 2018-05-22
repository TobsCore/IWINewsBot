package hska.iwi.telegramBot.ChatBot.Routines

import com.rivescript.RiveScript
import hska.iwi.telegramBot.mensa.MensaMoltke
import hska.iwi.telegramBot.service._
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

class MensaRoutine extends CustomSubroutine with Instances {

  implicit val jsonDefaultFormats: DefaultFormats.type = org.json4s.DefaultFormats

  override def call(rs: RiveScript, args: Array[String]): String = {
    val foodAdditives: Seq[String] = args.length match {
      case 2 =>
        getFoodAdditivesSeqByName(args(1))
      case 1 => getFoodAdditivesSeqByName(args(0))
      case _ => Seq()
    }

    logger.info("args.length: " + args.length)
    args.foreach(t => logger.info("Parameter: " + t))
    logger.info("Zusaetze: " + foodAdditives)

    val result = if (args.length > 1) {
      args.headOption match {
        case Some(param: String) =>
          callMensa(param, rs, foodAdditives)
        case _ =>
          val stringBuilder = new StringBuilder
          stringBuilder.append("Heute gibt es:\n")
          stringBuilder.append(mensaRequest(rs, 0).getOrElse("Keine Daten vorhanden."),
                               foodAdditives)
          stringBuilder.toString()
      }
    } else {
      callMensa("heute", rs, foodAdditives)
    }

    result

  }

  def callMensa(param: String, rs: RiveScript, foodAdditives: Seq[String] = Seq()): String = {
    val stringBuilder = new StringBuilder
    logger.info("Param: " + param)
    param.toLowerCase() match {
      case "heute" =>
        stringBuilder.append("Heute gibt es:\n")
        stringBuilder.append(mensaRequest(rs, 0, foodAdditives).getOrElse("Keine Daten vorhanden."))
        stringBuilder.toString()
      case "morgen" =>
        stringBuilder.append("Morgen gibt es:\n")
        stringBuilder.append(mensaRequest(rs, 1, foodAdditives).getOrElse("Keine Daten vorhanden."))
        stringBuilder.toString()
      case "übermorgen" =>
        stringBuilder.append("Übermorgen gibt es:\n")
        stringBuilder.append(mensaRequest(rs, 2, foodAdditives).getOrElse("Keine Daten vorhanden."))
        stringBuilder.toString()
      case "gestern" =>
        stringBuilder.append("Gestern gab es:\n")
        stringBuilder.append(
          mensaRequest(rs, -1, foodAdditives).getOrElse("Keine Daten vorhanden."))
        stringBuilder.toString()
      case "vorgestern" =>
        stringBuilder.append("Vorgestern gab es:\n")
        stringBuilder.append(
          mensaRequest(rs, -2, foodAdditives).getOrElse("Keine Daten vorhanden."))
        stringBuilder.toString()
      case _ =>
        val foodOutput = getFoodAdditivesSeqByName(param)
        if (foodOutput.isEmpty) {
          "Das habe ich leider nicht verstanden. Möchtest du wissen, was es heute, morgen oder übermorgen in der Mensa gibt? Für weitere Tage rufe /mensa auf."
        } else {
          stringBuilder.append("Heute gibt es:\n")
          stringBuilder.append(mensaRequest(rs, 0, foodOutput).getOrElse("Keine Daten vorhanden."))
          stringBuilder.toString()
        }

    }
  }

  def mensaRequest(rs: RiveScript,
                   daysInFuture: Int,
                   foodAdditives: Seq[String] = Seq()): Option[String] = {
    val mensaUrl = FeedURL.mensa + LocalDateTime.getDateInFuture(daysInFuture)
    val content = HTTPGet.get(mensaUrl)

    if (content.isDefined) {
      //parses the json entries and stores them in a MensaMoltke object
      val mensa = JsonMethods.parse(content.get).extract[MensaMoltke]
      val userID = UserID(rs.currentUser().toInt)
      val priceConfig = redis.getPriceConfigForUser(userID)
      Some(mensa.toString(daysInFuture, priceConfig, foodAdditives))
    } else {
      None
    }
  }

  def getFoodAdditivesSeqByName(param: String): Seq[String] =
    param.toLowerCase match {
      case "vegan" | "veganes" | "ohne tierische produkte" => FoodAdditives.Vegan
      case "vegetarisch" | "vegetarisches" | "ohne fleisch" | "fleischlos" =>
        FoodAdditives.Vegetarian
      case "mit schwein" | "schwein" | "pork"                => FoodAdditives.Pork
      case "rind" | "kuh" | "beef" | "mit rind" | "mit beef" => FoodAdditives.Beef
      case "mit fleisch" | "fleisch" =>
        FoodAdditives.Beef.toSet.union(FoodAdditives.Pork.toSet).toSeq
      case "schalentiere" | "weichtiere" | "meeresfrüchte" | "mit schalentieren" |
          "mit weichtieren" | "mit meeresfrüchten" =>
        FoodAdditives.Molluscs
      case "fisch" | "mit fisch" => FoodAdditives.Fish
      case "mit tierischen produkten" =>
        FoodAdditives.Fish.toSet
          .union(FoodAdditives.Beef.toSet)
          .union(FoodAdditives.Molluscs.toSet)
          .union(FoodAdditives.Pork.toSet)
          .toSeq
      case _ => Seq()

    }
}

object FoodAdditives {
  val Beef = Seq("93", "94")
  val Fish = Seq("Fi", "27", "98")
  val Pork = Seq("95")
  val Vegetarian = Seq("96")
  val Vegan = Seq("97")
  val Molluscs = Seq("We")
}
