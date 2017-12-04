package hska.iwi.telegramBot.service

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, Locale}

object LocalDate {

  def getCurrentDate(): String = {
    val format = new SimpleDateFormat("yyyy-MM-dd")
    format.format(Calendar.getInstance().getTime())
  }

  def formatPretty(date: Date): String = {
    val format = new SimpleDateFormat("EEEE, d. MMM yyyy", Locale.GERMANY)
    format.format(date)
  }

  def formatPrettyCurrentDate(): String = {
    val currentDate = Calendar.getInstance().getTime()
    formatPretty(currentDate)
  }

  def getWeekDay(day: Int): String = {
    val weekDays =
      Seq("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag")
    weekDays(day)
  }

}
