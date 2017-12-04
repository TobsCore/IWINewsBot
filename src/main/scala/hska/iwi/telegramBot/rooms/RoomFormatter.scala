package hska.iwi.telegramBot.rooms

import java.util.Locale

import hska.iwi.telegramBot.news.Entry
import hska.iwi.telegramBot.service.{LocalDate, LocalTime}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object RoomFormatter {

  def format(entry: FreeRoomEntry): String = {
    val date = LocalDate.formatPrettyCurrentDate()
    val startTime = LocalTime.prettyHourIntervall(entry.startTime)
    val endTime = LocalTime.prettyHourIntervall(entry.endTime)

    s"""<b>Freie Räume</b>
       |$date
       |von $startTime
       |bis $endTime
       |
       |${formatLocations(entry.locations)}""".stripMargin
  }

  def formatLocations(locations: Set[Room]): String = {
    val locationsString: StringBuilder = new StringBuilder()
    var lastBuilding = ""
    for (room <- locations) {
      var currentBuilding = room.building
      if (currentBuilding != lastBuilding) {
        locationsString.append(s"Gebäude $currentBuilding: \n")
      }
      locationsString.append(s"&#8226; Raum ${room.room}")
      if (room.aliasName != null) locationsString.append(s" (${room.aliasName})\n")
      else locationsString.append("\n")
      lastBuilding = currentBuilding
    }
    locationsString.toString()
  }
}
