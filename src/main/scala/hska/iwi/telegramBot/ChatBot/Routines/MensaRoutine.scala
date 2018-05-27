package hska.iwi.telegramBot.ChatBot.Routines

import com.rivescript.RiveScript
import hska.iwi.telegramBot.mensa.MensaMoltke
import hska.iwi.telegramBot.service._
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

class MensaRoutine extends CustomSubroutine with Instances {

  implicit val jsonDefaultFormats: DefaultFormats.type = org.json4s.DefaultFormats

  override def call(rs: RiveScript, args: Array[String]): String = {

    val concatStringMit = concatExpressions(args, "mit")
    val concatStringOhne = concatExpressions(args, "ohne")
    val day = args.filter(arg => DaySynonyms.AllDays.contains(arg))
    
    day.foreach(d => logger.debug("Days: " + d))
    args.foreach(arg => logger.debug("Args: " + arg))

    val containsAnd = splitExpressions(args, "und")

    val foodAdditives = if (containsAnd.isDefined) {
      getFoodAdditivesUnionWithAnd(containsAnd.get(0), containsAnd.get(1))
    } else {

      args.length match {
        case 0 => Seq()
        case 1 =>
          if (concatStringMit.isDefined) {
            logger.debug("concatMit: " + concatStringMit.get)
            getFoodAdditivesSeqByName(concatStringMit.get)
          } else if (concatStringOhne.isDefined) {
            logger.debug("concatOhne: " + concatStringOhne.get)
            getFoodAdditivesSeqByName(concatStringOhne.get)
          } else {
            getFoodAdditivesSeqByName(args(0))
          }
        case 2 =>
          if (concatStringMit.isDefined) {
            logger.debug("concatMit: " + concatStringMit.get)
            getFoodAdditivesSeqByName(concatStringMit.get)
          } else if (concatStringOhne.isDefined) {
            logger.debug("concatOhne: " + concatStringOhne.get)
            getFoodAdditivesSeqByName(concatStringOhne.get)
          } else {
            if (DaySynonyms.AllDays.contains(args(0))) {
              getFoodAdditivesSeqByName(args(1))
            } else {
              getFoodAdditivesSeqByName(args(0))
            }
          }
        case _ =>
          if (concatStringMit.isDefined) {
            logger.debug("concatMit: " + concatStringMit.get)
            getFoodAdditivesSeqByName(concatStringMit.get)
          } else if (concatStringOhne.isDefined) {
            logger.debug("concatOhne: " + concatStringOhne.get)
            getFoodAdditivesSeqByName(concatStringOhne.get)
          } else {
            getFoodAdditivesSeqByName(args.head)
          }
      }
    }
    logger.debug("FoodAdditivies: " + foodAdditives)

    args.headOption match {
      case Some(param) =>
        if (day.length > 0) {
          callMensa(day(0), rs, foodAdditives)
        } else {
          callMensa(DaySynonyms.Today, rs, foodAdditives)
        }
      case _ =>
        callMensa(DaySynonyms.Today, rs, foodAdditives)
    }
  }

  def callMensa(param: String, rs: RiveScript, foodAdditives: Seq[String] = Seq()): String = {
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
        val foodOutput = getFoodAdditivesSeqByName(param)
        if (foodOutput.isEmpty) {
          "Das habe ich leider nicht verstanden. Möchtest du wissen, was es heute, morgen oder " +
            "übermorgen in der Mensa gibt? Für weitere Tage rufe /mensa auf."
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
      case "vegan" | "veganes" | "vegane" | "veganen" | "ohne tierische produkte" =>
        FoodAdditives.Vegan
      case "vegetarisch" | "vegetarisches" | "vegetarischen" | "vegetarische" =>
        FoodAdditives.Vegetarian
      case "ohne fleisch" | "fleischlos" | "ohne tier" | "tierlos" =>
        FoodAdditives.Vegan.toSet.union(FoodAdditives.Vegetarian.toSet).toSeq
      case "mit schwein" | "schwein" | "pork"                => FoodAdditives.Pork
      case "rind" | "kuh" | "beef" | "mit rind" | "mit beef" => FoodAdditives.Beef
      case "mit fleisch" | "fleisch" | "fleischhaltiges" | "fleischhaltigen" =>
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

  def getFoodAdditivesUnionWithAnd(param1: String, param2: String): Seq[String] = {
    logger.debug("Foodparameter which should be joined")
    logger.debug("Parameter1 " + param1)
    logger.debug("Paramter2 " + param2)
    val food1 = getFoodAdditivesSeqByName(param1)
    val food2 = getFoodAdditivesSeqByName(param2)

    food1.union(food2)
  }

  def splitExpressions(expressions: Array[String], searchString: String): Option[Array[String]] = {
    val index = expressions.indexOf(searchString)
    val resultArray = Array.fill[String](2)("")

    if (index != -1) {
      var slicedArray1 = expressions.slice(0, index)
      slicedArray1.foreach(elem => {
        for (day <- DaySynonyms.AllDays) {
          if (day == elem) {
            slicedArray1 = slicedArray1.slice(slicedArray1.indexOf(elem), expressions.length)
          }
        }
      })

      var slicedArray2 = expressions.slice(index + 1, expressions.length)
      slicedArray2.foreach(elem => {
        for (day <- DaySynonyms.AllDays) {
          if (day == elem) {
            slicedArray2 = slicedArray2.slice(0, slicedArray2.indexOf(elem))
          }
        }
      })
      resultArray(1) = slicedArray2.mkString(" ")
      resultArray(0) = slicedArray1.mkString(" ")
      Some(resultArray)
    } else {
      None
    }
  }

  def concatExpressions(expressions: Array[String], searchString: String): Option[String] = {
    val index = expressions.indexOf(searchString)

    if (index != -1) {
      var slicedArray = expressions.slice(index, expressions.length)
      slicedArray.foreach(elem => {
        for (day <- DaySynonyms.AllDays) {
          if (day == elem) {
            slicedArray = slicedArray.slice(0, slicedArray.indexOf(elem))
          }
        }
      })
      logger.debug("SlicedArray: " + slicedArray.mkString(" "))
      Some(slicedArray.mkString(" "))
    } else {
      None
    }
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

object DaySynonyms {
  val Tomorrow = "morgen"
  val TheDayAfterTomorrow = "übermorgen"
  val Today = "heute"
  val Yesterday = "gestern"
  val TheDayBeforeYesterday = "vorgestern"

  val AllDays = Seq(TheDayBeforeYesterday, Yesterday, Today, TheDayAfterTomorrow, Tomorrow)
}
