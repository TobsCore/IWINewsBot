package hska.iwi.telegramBot.service

import java.text.SimpleDateFormat
import java.util.{Calendar, Locale}

object LocalTime {

  def prettyHourFormat(): String = {
    val time = Calendar.getInstance().getTime
    val format = new SimpleDateFormat("HH:mm", Locale.GERMANY)
    format.format(time)
  }

  def prettyHourIntervall(time: Int): String = {
    val hours = time / 60
    val minutes = time % 60
    val timeString: StringBuilder = new StringBuilder()
    timeString.append("%02d".format(hours) + ":" + "%02d".format(minutes) + " Uhr")
    timeString.toString()
  }

}
