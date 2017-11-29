package hska.iwi.telegramBot.mensa

import java.util.{Currency, Locale}

object MensaFormatter {

  def format(mensa: MensaMoltke): String = {
    val date = Date.formatPrettyCurrentDate()
    val formattedMealGroups: String = formatMealGroups(mensa.mealGroups)

    s"""<b>${mensa.name}</b>
       |$date
       |
       |$formattedMealGroups
       |<i>Preise: Studierende / Mitarbeiter(innen)</i>
       |""".stripMargin
  }

  private def formatMealGroups(mealGroups: Set[MealGroup]): String = {
    var formattedGroups: String = ""
    for (mealGroup <- mealGroups) {
      val meals = formatMeals(mealGroup.meals)
      formattedGroups +=
        s"""<b>${mealGroup.title}</b>
         |$meals
         |""".stripMargin
    }
    formattedGroups
  }

  private def formatMeals(meals: Set[Meal]): String = {
    val deCurrency = Currency.getInstance(new Locale("de", "DE"))
    val formatter = java.text.NumberFormat.getCurrencyInstance
    formatter.setCurrency(deCurrency)

    var formattedMeals = ""
    for (meal <- meals) {
      formattedMeals +=
        s"""${meal.name} <i>${getEmojiSet(meal)}</i>
         |${formatter.format(meal.priceStudent)} / ${formatter.format(meal.priceEmployee)}
         |""".stripMargin
    }
    formattedMeals
  }

  private def getEmojiSet(meal: Meal): String = {
    var emojiString = ""
    for (food <- meal.foodAdditiveNumbers) {
      food match {
        case "Fi" | "27" | "98" => emojiString += "\ud83d\udc1f " //Fisch
        case "93" | "94"        => emojiString += "\ud83d\udc04 " //Rind
        case "95"               => emojiString += "\ud83d\udc16 " //Schwein
        case "96"               => emojiString += "(veget.) " //vegetarisch
        case "97"               => emojiString += "(vegan) " //vegan
        case _                  => emojiString += ""
      }
    }
    emojiString
  }

}
