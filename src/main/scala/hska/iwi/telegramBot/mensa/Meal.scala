package hska.iwi.telegramBot.mensa

case class Meal(name: String,
                identifier: String,
                foodAdditiveNumbers: Set[String],
                priceStudent: Double,
                priceGuest: Double,
                priceEmployee: Double,
                pricePupil: Double,
                priceAdditive: String) {}
