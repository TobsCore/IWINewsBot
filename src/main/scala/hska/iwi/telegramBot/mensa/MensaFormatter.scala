package hska.iwi.telegramBot.mensa

import java.util.{Currency, Locale}

object MensaFormatter {

  def format(mensa: MensaMoltke): String = {
    val date = Date.formatPrettyCurrentDate()
    val formattedMealGroups: String = formatMealGroups(mensa.mealGroups)

    s"""<b>${mensa.name}</b>
       |<b>Heute: $date</b>
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
        s"""${meal.name}
         |<i>${formatter.format(meal.priceStudent)} / ${formatter.format(meal.priceEmployee)}</i>
         |""".stripMargin
    }
    formattedMeals
  }

}
