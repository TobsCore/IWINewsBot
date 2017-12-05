package hska.iwi.telegramBot.lecturers

import hska.iwi.telegramBot.service.{LocalDateTime}

object LecturerFormatter {

  def format(lecturer: Lecturer): String = {
    s"""<b>${lecturer.fullname}</b>
       |${lecturer.mailAddress}${roomAndBuilding(lecturer)}${consultationhours(lecturer)}
       |${lecturer.consultationTimeComment match {
         case "" => ""
         case _  => "\n" + lecturer.consultationTimeComment
       }}
       |""".stripMargin
  }

  private def consultationhours(lecturer: Lecturer): String = {
    val sprechzeiten
      : Boolean = lecturer.consultationDay != -1 && lecturer.consultationStartTime != -1 && lecturer.consultationEndTime != -1
    if (sprechzeiten) {
      var day = LocalDateTime.getWeekDay(lecturer.consultationDay)
      val startTime = LocalDateTime.prettyHourIntervall(lecturer.consultationStartTime)
      val endTime = LocalDateTime.prettyHourIntervall(lecturer.consultationEndTime)
      s"""|
         |
         |<b>Sprechzeiten:</b>
         |$day, ${startTime} - ${endTime} Uhr""".stripMargin
    } else ""
  }

  private def roomAndBuilding(lecturer: Lecturer): String = {
    if (lecturer.building != "" && lecturer.room != "") {
      s"""|
         |Raum: ${lecturer.building}${lecturer.room}""".stripMargin
    } else ""
  }

}
