package hska.iwi.telegramBot.rooms

import hska.iwi.telegramBot.service.LocalDateTime

case class FreeRoomEntry(day: Int, endTime: Int, locations: Set[Room], startTime: Int) {

  override def toString: String = {
    val date = LocalDateTime.formatPrettyCurrentDate()

    s"""<b>Freie Räume</b>
       |$date${formatLocations(this)}""".stripMargin
  }

  private def formatLocations(entry: FreeRoomEntry): String = {
    val locations = entry.locations
    if (locations.isEmpty) {
      "Im Moment sind keine Räume verfügbar."
    } else {
      val startTime = LocalDateTime.prettyHourIntervall(entry.startTime)
      val endTime = LocalDateTime.prettyHourIntervall(entry.endTime)

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
