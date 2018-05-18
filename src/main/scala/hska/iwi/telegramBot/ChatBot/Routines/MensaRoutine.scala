package hska.iwi.telegramBot.ChatBot.Routines

import com.rivescript.RiveScript
import hska.iwi.telegramBot.mensa.MensaMoltke
import hska.iwi.telegramBot.service._
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

class MensaRoutine extends CustomSubroutine with Instances {

  implicit val jsonDefaultFormats: DefaultFormats.type = org.json4s.DefaultFormats

  override def call(rs: RiveScript, args: Array[String]): String = {
    val param2 = args.lift(2).getOrElse("")
    val foodAdditives = getFoodAdditivesSeqByName(param2)
    args.headOption match {
      case Some(param: String) =>
        val parameter = param
        callMensa(param, rs, foodAdditives)
      case _ =>
        val stringBuilder = new StringBuilder
        stringBuilder.append("Heute gibt es:\n")
        stringBuilder.append(mensaRequest(rs, 0).getOrElse("Keine Daten vorhanden."), foodAdditives)
        stringBuilder.toString()
    }
  }

  private def callMensa(param: String,
                        rs: RiveScript,
                        foodAdditives: Seq[String] = Seq()): String = {
    val stringBuilder = new StringBuilder
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
        "Das habe ich leider nicht verstanden. Möchtest du wissen, was es heute, morgen oder übermorgen in der Mensa gibt? Für weitere Tage rufe /mensa auf."

    }
  }

  private def mensaRequest(rs: RiveScript,
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

  private def getFoodAdditivesSeqByName(param: String): Seq[String] =
    param.toLowerCase match {
      case "vegan|veganes|ohne tierische Produkte"             => FoodAdditives.Vegan
      case "vegetarisch|vegetarisches|ohne Fleisch|fleischlos" => FoodAdditives.Vegetarian
      case "rind|kuh|beef|mit rind|mit schwein|mit beef"       => FoodAdditives.Beef
      case "mit fleisch|fleisch"                               => FoodAdditives.Beef.toSet.union(FoodAdditives.Pork.toSet).toSeq
      case "schalentiere|weichtiere|meeresfrüchte|mit schalentieren|mit weichtieren|mit meeresfrüchten" =>
        FoodAdditives.Molluscs
      case "fisch| mit fisch" => FoodAdditives.Fish
      case "mit tierischen produkten" =>
        FoodAdditives.Fish.toSet
          .union(
            FoodAdditives.Beef.toSet.union(
              FoodAdditives.Molluscs.toSet.union(FoodAdditives.Pork.toSet)))
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
