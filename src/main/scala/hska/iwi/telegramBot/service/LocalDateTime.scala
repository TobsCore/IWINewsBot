package hska.iwi.telegramBot.service

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, Locale}

import org.joda.time.format.DateTimeFormat

object LocalDateTime {

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

  def parseTimestamp(timestamp: String): String = {
    val timestampFormatted = timestamp.dropRight(4)
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withLocale(Locale.GERMANY)
    val dt = formatter.parseDateTime(timestampFormatted)
    dt.toString("EEEE, d. MMM. yyyy - hh:mm 'Uhr'")
  }

  def prettyHourFormat(): String = {
    val time = Calendar.getInstance().getTime
    val format = new SimpleDateFormat("HH:mm", Locale.GERMANY)
    format.format(time)
  }

  def prettyHourIntervall(time: Int): String = {
    val hours = time / 60
    val minutes = time % 60
    val timeString: StringBuilder = new StringBuilder()
    timeString.append("%02d".format(hours) + ":" + "%02d".format(minutes))
    timeString.toString()
  }

}
