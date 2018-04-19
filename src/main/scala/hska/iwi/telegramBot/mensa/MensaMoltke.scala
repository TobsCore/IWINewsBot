package hska.iwi.telegramBot.mensa

import java.text.NumberFormat
import java.util.Locale

import hska.iwi.telegramBot.service.{LocalDateTime, PriceConfig}

case class MensaMoltke(name: String, mealGroups: Seq[MealGroup], status: String, date: String) {

  def toString(daysInFuture: Int, priceConfig: PriceConfig): String = {
    val date = LocalDateTime.formatPrettyDateInFuture(daysInFuture)
    val formattedMealGroups: String = formatMealGroups(mealGroups.sortBy(_.title), priceConfig)

    val config = priceConfig.toString

    s"""<b>${this.name}</b>
       |$date
       |
       |Preise für $config,
       |zum Ändern /settings aufrufen.
       |
       |""".stripMargin + {
      if (formattedMealGroups.isEmpty) {
        "Kein Speiseplan verfügbar"
      } else {
        formattedMealGroups
      }
    }
  }

  private def formatMealGroups(mealGroups: Seq[MealGroup], priceConfig: PriceConfig): String = {
    val formattedGroups: StringBuilder = new StringBuilder()
    for (mealGroup <- mealGroups) {
      if (mealGroup.meals.isEmpty) {
        formattedGroups.append(s"""<b>${formatTitle(mealGroup.title)}</b>
                                  |geschlossen
                                  |
                                  |""".stripMargin)
      } else {
        val meals = formatMeals(mealGroup.meals.sortBy(_.priceStudent).reverse, priceConfig)
        formattedGroups.append(s"""<b>${formatTitle(mealGroup.title)}</b>
             |$meals
             |""".stripMargin)
      }
    }
    formattedGroups.toString
  }

  private def formatTitle(title: String): String = {
    title match {
      case "Gut&Guenstig" => "Gut & Günstig"
      case _              => title
    }
  }

  private def formatMeals(meals: Seq[Meal], priceConfig: PriceConfig): String = {
    val locale = Locale.GERMAN
    val formatter = NumberFormat.getNumberInstance(locale)
    formatter.setMaximumFractionDigits(2)
    formatter.setMinimumFractionDigits(2)

    val formattedMeals: StringBuilder = new StringBuilder()
    if (priceConfig.configValue == "student") {
      for (meal <- meals) {
        formattedMeals.append(
          s"""${meal.name} <i>${getEmojis(meal)}</i> ${formatter.format(meal.priceStudent)}€
               |""".stripMargin)
      }
    } else if (priceConfig.configValue == "employee") {
      for (meal <- meals) {
        formattedMeals.append(
          s"""${meal.name} <i>${getEmojis(meal)}</i> ${formatter.format(meal.priceEmployee)}€
               |""".stripMargin)
      }
    } else {
      for (meal <- meals) {
        formattedMeals.append(s"""${meal.name} <i>${getEmojis(meal)}</i> ${formatter.format(
                                   meal.priceStudent)}€ / ${formatter.format(meal.priceEmployee)}€
                 |""".stripMargin)
      }
    }
    formattedMeals.toString
  }

  private def getEmojis(meal: Meal): String = {
    val emojiString: StringBuilder = new StringBuilder()
    for (food <- meal.foodAdditiveNumbers) {
      food match {
        case "Fi" | "27" | "98" =>
          if (!emojiString.toString.contains("\uD83D\uDC1F")) {
            emojiString.append("\uD83D\uDC1F ")
          } //fish
        case "93" | "94" =>
          if (!emojiString.toString.contains("\ud83d\udc04")) {
            emojiString.append("\ud83d\udc04 ")
          } //beef
        case "95" => emojiString.append("\ud83d\udc16 ") //pork
        case "96" => emojiString.append("(veget.) ") //vegetarian
        case "97" => emojiString.append("(vegan) ") //vegan
        case "We" => emojiString.append("\uD83E\uDD91 ") //molluscs
        case _    => emojiString.append("")
      }
    }
    emojiString.toString
  }

}
