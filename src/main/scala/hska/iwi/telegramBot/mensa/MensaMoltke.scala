package hska.iwi.telegramBot.mensa

import java.util.{Currency, Locale}

import hska.iwi.telegramBot.service.LocalDateTime

case class MensaMoltke(name: String, mealGroups: Set[MealGroup], status: String, date: String) {

  def toString(daysInFuture: Int): String = {
    val date = daysInFuture match {
      case 0 => LocalDateTime.formatPrettyCurrentDate()
      case 1 => LocalDateTime.formatPrettyDateInFuture(1)
      case _ => LocalDateTime.formatPrettyDateInFuture(2)
    }
    val formattedMealGroups: String = formatMealGroups(this.mealGroups)
    s"""<b>${this.name}</b>
       |$date
       |
       |""".stripMargin + {
      if (formattedMealGroups.isEmpty) {
        "Kein Speiseplan verfügbar"
      } else {
        s"""$formattedMealGroups
           |<i>Preise: Studierende / Mitarbeiter(innen)</i>
           |""".stripMargin
      }
    }
  }

  private def formatMealGroups(mealGroups: Set[MealGroup]): String = {
    val formattedGroups: StringBuilder = new StringBuilder()
    for (mealGroup <- mealGroups) {
      val meals = formatMeals(mealGroup.meals)
      formattedGroups.append(s"""<b>${mealGroup.title}</b>
                           |$meals
                           |""".stripMargin)

    }
    formattedGroups.toString()
  }

  private def formatMeals(meals: Set[Meal]): String = {
    val deCurrency = Currency.getInstance(new Locale("de", "DE"))
    val formatter = java.text.NumberFormat.getCurrencyInstance
    formatter.setCurrency(deCurrency)

    val formattedMeals: StringBuilder = new StringBuilder()
    for (meal <- meals) {
      formattedMeals.append(s"""${meal.name} <i>${getEmojis(meal)}</i>
         |${formatter.format(meal.priceStudent)} / ${formatter.format(meal.priceEmployee)}
         |""".stripMargin)
    }
    formattedMeals.toString()
  }

  private def getEmojis(meal: Meal): String = {
    val emojiString: StringBuilder = new StringBuilder()
    for (food <- meal.foodAdditiveNumbers) {
      food match {
        case "Fi" | "27" | "98" => {
          if (!emojiString.toString().contains("\uD83D\uDC1F")) {
            emojiString.append("\uD83D\uDC1F ")
          }
        } //Fisch
        case "93" | "94" =>
          if (!emojiString.toString().contains("\ud83d\udc04")) {
            emojiString.append("\ud83d\udc04 ")
          } //Rind
        case "95" => emojiString.append("\ud83d\udc16 ") //Schwein
        case "96" => emojiString.append("(veget.) ") //vegetarisch
        case "97" => emojiString.append("(vegan) ") //vegan
        case "We" => emojiString.append("\uD83E\uDD91 ")
        case _    => emojiString.append("")
      }
    }
    emojiString.toString()
  }

}
