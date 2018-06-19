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
    val day = args.filter(arg => DaysAndSynonyms.AllDays.contains(arg))

    day.foreach(d => logger.debug("Days: " + d))
    args.foreach(arg => logger.debug("Args: " + arg))

    val containsAnd = splitExpressions(args, "und")

    val foodAdditives = if (containsAnd.isDefined) {
      if (concatStringOhne.isDefined && !concatStringMit.isDefined) {
        getFoodAdditivesUnionOrInteresectWithAnd(containsAnd.get(0), containsAnd.get(1), "ohne")
      } else {
        getFoodAdditivesUnionOrInteresectWithAnd(containsAnd.get(0), containsAnd.get(1))
      }

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
            if (DaysAndSynonyms.AllDays.contains(args(0))) {
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
      case Some(_) =>
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
    param.toLowerCase() match {
      case DaySynonyms.Today =>
        mensaRequest(rs, 0, foodAdditives).getOrElse("Keine Daten vorhanden.")
      case DaySynonyms.Tomorrow =>
        mensaRequest(rs, 1, foodAdditives).getOrElse("Keine Daten vorhanden.")
      case DaySynonyms.TheDayAfterTomorrow =>
        mensaRequest(rs, 2, foodAdditives).getOrElse("Keine Daten vorhanden.")
      case DaySynonyms.Yesterday =>
        mensaRequest(rs, -1, foodAdditives).getOrElse("Keine Daten vorhanden.")
      case DaySynonyms.TheDayBeforeYesterday =>
        mensaRequest(rs, -2, foodAdditives).getOrElse("Keine Daten vorhanden.")
      case Days.Mon =>
        mensaRequest(rs, LocalDateTime.getDaysInfutureWithWantedWeekDay(1), foodAdditives)
          .getOrElse("Keine Daten vorhanden.")
      case Days.Tue =>
        mensaRequest(rs, LocalDateTime.getDaysInfutureWithWantedWeekDay(2), foodAdditives)
          .getOrElse("Keine Daten vorhanden.")
      case Days.Wed =>
        mensaRequest(rs, LocalDateTime.getDaysInfutureWithWantedWeekDay(3), foodAdditives)
          .getOrElse("Keine Daten vorhanden.")
      case Days.Thu =>
        mensaRequest(rs, LocalDateTime.getDaysInfutureWithWantedWeekDay(4), foodAdditives)
          .getOrElse("Keine Daten vorhanden.")
      case Days.Fri =>
        mensaRequest(rs, LocalDateTime.getDaysInfutureWithWantedWeekDay(5), foodAdditives)
          .getOrElse("Keine Daten vorhanden.")
      case Days.Sat =>
        mensaRequest(rs, LocalDateTime.getDaysInfutureWithWantedWeekDay(6), foodAdditives)
          .getOrElse("Keine Daten vorhanden.")
      case Days.Sun =>
        mensaRequest(rs, LocalDateTime.getDaysInfutureWithWantedWeekDay(7), foodAdditives)
          .getOrElse("Keine Daten vorhanden.")
      case _ =>
        val foodOutput = getFoodAdditivesSeqByName(param)
        if (foodOutput.isEmpty) {
          "Das habe ich leider nicht verstanden. Möchtest du wissen, was es heute, morgen oder " +
            "übermorgen in der Mensa gibt? Für weitere Tage rufe /mensa auf."
        } else {
          mensaRequest(rs, 0, foodOutput).getOrElse("Keine Daten vorhanden.")
        }

    }
  }

  def mensaRequest(rs: RiveScript,
                   daysInFuture: Int,
                   foodAdditives: Seq[String] = Seq()): Option[String] = {
    val mensaUrl = FeedURL.mensa + LocalDateTime.getDateInFuture(daysInFuture)
    val content = HTTPGet.cacheGet(mensaUrl)

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
      case "mit schwein" | "schwein" | "pork" => FoodAdditives.Pork
      case "rind" | "kuh" | "beef" | "mit rind" | "mit beef" => FoodAdditives.Beef
      case "mit fleisch" | "fleisch" | "fleischhaltiges" | "fleischhaltigen" | "ohne gemüse" =>
        FoodAdditives.Beef.toSet.union(FoodAdditives.Pork.toSet).toSeq
      case "schalentiere" | "weichtiere" | "meeresfrüchte" | "mit schalentieren" |
           "mit weichtieren" | "mit meeresfrüchten" =>
        FoodAdditives.Molluscs
      case "fisch" | "mit fisch" => FoodAdditives.Fish.union(FoodAdditives.Molluscs)
      case "mit tierischen produkten" =>
        FoodAdditives.Fish
          .union(FoodAdditives.Beef)
          .union(FoodAdditives.Molluscs)
          .union(FoodAdditives.Pork)
      case "ohne fleisch" | "fleischlos" | "fleischloses" | "ohne tier" | "tierlos" | "tierloses"
           | "gemüse" =>
        FoodAdditives.Vegan.union(FoodAdditives.Vegetarian)
      case "ohne fisch" | "fischlos" | "fischloses" => FoodAdditives.All.filterNot(_ ==
        FoodAdditives.Fish)
      case "ohne schwein" => FoodAdditives.All.filterNot(FoodAdditives.Pork.contains(_))
      case "ohne rind" => FoodAdditives.All.filterNot(FoodAdditives.Beef.contains(_))
      case "ohne vegan" => FoodAdditives.All.filter(FoodAdditives.Vegan.contains(_))
      case "ohne vegetarisch" => FoodAdditives.All.filter(FoodAdditives.Vegetarian.contains(_))
      case _ => Seq()

    }

  def getFoodAdditivesUnionOrInteresectWithAnd(param1: String, param2: String,
                                               withOrWithout: String = "")
  : Seq[String] = {
    logger.debug("Foodparameter which should be joined or interesected")
    logger.debug("Parameter1 " + param1)
    logger.debug("Parameter2 " + param2)
    val food1 = getFoodAdditivesSeqByName(param1)
    val food2 = getFoodAdditivesSeqByName(param2)

    logger.debug("food1 " + food1)
    logger.debug("food2 " + food2)
    logger.debug("withOrWithout" + withOrWithout)

    withOrWithout match {
      case "ohne" => food1.intersect(food2)
      case _ => food1.union(food2).distinct
    }
  }

  def splitExpressions(expressions: Array[String], searchString: String): Option[Array[String]] = {
    val index = expressions.indexOf(searchString)
    val resultArray = Array.fill[String](2)("")

    if (index != -1) {
      var slicedArray1 = expressions.slice(0, index)
      slicedArray1.foreach(elem => {
        for (day <- DaysAndSynonyms.AllDays) {
          if (day == elem) {
            slicedArray1 = slicedArray1.slice(slicedArray1.indexOf(elem) + 1, expressions.length)
          }
        }
      })

      var slicedArray2 = expressions.slice(index + 1, expressions.length)
      slicedArray2.foreach(elem => {
        for (day <- DaysAndSynonyms.AllDays) {
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
        for (day <- DaysAndSynonyms.AllDays) {
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
  val All: Seq[String] = Beef.union(Fish).union(Pork).union(Vegetarian).union(Vegan).union(Molluscs)
}

object DaySynonyms {
  val Tomorrow = "morgen"
  val TheDayAfterTomorrow = "übermorgen"
  val Today = "heute"
  val Yesterday = "gestern"
  val TheDayBeforeYesterday = "vorgestern"

  val AllDays = Seq(TheDayBeforeYesterday, Yesterday, Today, TheDayAfterTomorrow, Tomorrow)
}

object Days {
  val Mon = "montag"
  val Tue = "dienstag"
  val Wed = "mittwoch"
  val Thu = "donnerstag"
  val Fri = "freitag"
  val Sat = "samstag"
  val Sun = "sonntag"

  val AllDays = Seq(Mon, Tue, Wed, Thu, Fri, Sat, Sun)
}

object DaysAndSynonyms {
  val AllDays: Seq[String] = DaySynonyms.AllDays.union(Days.AllDays)
}
