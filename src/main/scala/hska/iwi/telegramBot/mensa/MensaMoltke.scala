package hska.iwi.telegramBot.mensa

import java.text.NumberFormat
import java.util.Locale

import hska.iwi.telegramBot.service.{LocalDateTime, PriceConfig}

case class MensaMoltke(name: String, mealGroups: Seq[MealGroup], status: String, date: String) {

  def toString(daysInFuture: Int,
               priceConfig: PriceConfig,
               foodAdditives: Seq[String] = Seq()): String = {
    val date = LocalDateTime.formatPrettyDateInFuture(daysInFuture)
    val formattedMealGroups: String =
      formatMealGroups(mealGroups.sortBy(_.title), priceConfig, foodAdditives)

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

  private def formatMealGroups(mealGroups: Seq[MealGroup],
                               priceConfig: PriceConfig,
                               foodAdditives: Seq[String] = Seq()): String = {
    val formattedGroups: StringBuilder = new StringBuilder()
    for (mealGroup <- mealGroups) {
      if (mealGroup.meals.isEmpty) {
        formattedGroups.append(s"""<b>${formatTitle(mealGroup.title)}</b>
                                  |geschlossen
                                  |
                                  |""".stripMargin)
      } else {
        val meals =
          formatMeals(mealGroup.meals.sortBy(_.priceStudent).reverse, priceConfig, foodAdditives)
        if (!meals.isEmpty) {
          formattedGroups.append(s"""<b>${formatTitle(mealGroup.title)}</b>
                                    |$meals
                                    |""".stripMargin)
        }
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

  private def formatMeals(meals: Seq[Meal],
                          priceConfig: PriceConfig,
                          foodAdditives: Seq[String] = Seq()): String = {

    val formattedMeals: StringBuilder = new StringBuilder()
    for (meal <- meals) {
      if (foodAdditives.isEmpty) {
        formattedMeals.append(
          s"""${meal.name} <i>${getEmojis(meal)}</i> ${getCorrectPrice(meal, priceConfig)}
             |""".stripMargin)
      } else {
        val ingredients = foodAdditives.toSet.intersect(meal.foodAdditiveNumbers.toSet)
        for (ingredient <- ingredients) {
          if (!ingredient.isEmpty) {
            formattedMeals.append(
              s"""${meal.name} <i>${getEmojis(meal)}</i> ${getCorrectPrice(meal, priceConfig)}
                 |""".stripMargin)
          }
        }
      }
    }
    formattedMeals.toString()
  }

  private def getCorrectPrice(meal: Meal, priceConfig: PriceConfig): String = {
    val locale = Locale.GERMAN
    val formatter = NumberFormat.getNumberInstance(locale)
    formatter.setMaximumFractionDigits(2)
    formatter.setMinimumFractionDigits(2)
    val price = priceConfig.configValue match {
      case "student"  => formatter.format(meal.priceStudent) + "€"
      case "employee" => formatter.format(meal.priceEmployee) + "€"
      case _ =>
        Seq(meal.priceStudent, meal.priceEmployee)
        s"${formatter.format(meal.priceStudent)}€ / ${formatter.format(meal.priceEmployee)}€"
    }
    price
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
        case "95" | "100" => emojiString.append("\ud83d\udc16 ") //pork
        case "14" => emojiString.append("(Fleisch)")
        case "96" => emojiString.append("(veget.) ") //vegetarian
        case "97" => emojiString.append("(vegan) ") //vegan
        case "Kr" => emojiString.append("\uD83E\uDD80 ") //crab-like animals
        case "Wt" => emojiString.append("\uD83D\uDC1A ") //molluscs
        case _    => emojiString.append("")
      }
    }
    emojiString.toString
  }

}
