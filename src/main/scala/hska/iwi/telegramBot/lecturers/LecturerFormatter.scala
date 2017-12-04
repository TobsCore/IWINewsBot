package hska.iwi.telegramBot.lecturers

import hska.iwi.telegramBot.service.{LocalDate, LocalTime}

object LecturerFormatter {

  def format(lecturer: Lecturer): String = {
    s"""<b>${lecturer.fullname}</b>
       |${lecturer.mailAddress}${roomAndBuilding(lecturer)}${consultationhours(lecturer)}
       |${lecturer.consultationTimeComment match {
         case "" => ""
         case _  => lecturer.consultationTimeComment
       }}
       |""".stripMargin
  }

  private def consultationhours(lecturer: Lecturer): String = {
    val sprechzeiten
      : Boolean = lecturer.consultationDay != -1 && lecturer.consultationStartTime != -1 && lecturer.consultationEndTime != -1
    if (sprechzeiten) {
      var day = LocalDate.getWeekDay(lecturer.consultationDay)
      val startTime = LocalTime.prettyHourIntervall(lecturer.consultationStartTime)
      val endTime = LocalTime.prettyHourIntervall(lecturer.consultationEndTime)
      s"""|
         |
         |<b>Sprechzeiten:</b>
         |$day
         |von ${startTime}
         |bis ${endTime}
         |""".stripMargin
    } else ""
  }

  private def roomAndBuilding(lecturer: Lecturer): String = {
    if (lecturer.building != "" && lecturer.room != "") {
      s"""|
         |Raum: ${lecturer.building}${lecturer.room}""".stripMargin
    } else ""
  }

}
