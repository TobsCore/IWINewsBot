package hska.iwi.telegramBot.mensa

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

object Date {

  def getCurrentDate(): String = {
    val format = new SimpleDateFormat("yyyy-MM-dd")
    format.format(Calendar.getInstance().getTime())
  }

  def formatPretty(date: Date): String = {
    val format = new SimpleDateFormat("d. MMM yyyy")
    format.format(date)
  }

  def formatPrettyCurrentDate(): String = {
    val currentDate = Calendar.getInstance().getTime()
    formatPretty(currentDate)
  }

}
