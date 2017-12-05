package hska.iwi.telegramBot.rooms

import java.util.Locale

import hska.iwi.telegramBot.news.Entry
import hska.iwi.telegramBot.service.{LocalDate, LocalTime}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object RoomFormatter {

  def format(entry: FreeRoomEntry): String = {
    val date = LocalDate.formatPrettyCurrentDate()

    s"""<b>Freie Räume</b>
       |$date${formatLocations(entry)}""".stripMargin
  }

  def formatLocations(entry: FreeRoomEntry): String = {
    val locations = entry.locations
    if (locations.isEmpty) {
      "Im Moment sind keine Räume verfügbar."
    } else {
      val startTime = LocalTime.prettyHourIntervall(entry.startTime)
      val endTime = LocalTime.prettyHourIntervall(entry.endTime)

      val locationsString: StringBuilder = new StringBuilder()

      locationsString.append(s"""
           |$startTime - $endTime Uhr
           |
           |""".stripMargin)

      var lastBuilding = ""
      for (room <- locations) {
        var currentBuilding = room.building
        if (currentBuilding != lastBuilding) {
          locationsString.append(s"Gebäude $currentBuilding:\n")
        }
        locationsString.append(s"&#8226; Raum ${room.room}")
        if (room.aliasName != null) locationsString.append(s" (${room.aliasName})\n")
        else locationsString.append("\n")
        lastBuilding = currentBuilding
      }
      locationsString.toString()
    }
  }
}
