package hska.iwi.telegramBot.lecturers

import hska.iwi.telegramBot.service.LocalDateTime

case class Lecturer(id: Int,
                    firstname: String,
                    lastname: String,
                    title: String,
                    adsAccount: String,
                    mailAddress: String,
                    consultationDay: Int,
                    consultationStartTime: Int,
                    consultationEndTime: Int,
                    consultationTimeComment: String,
                    room: String,
                    building: String,
                    department: String,
                    faculty: String,
                    picture: String,
                    pictureType: String,
                    lecturers: Set[Lecture],
                    fullname: String,
                    visitingLecturer: Boolean,
                    shortenedFullname: String) {

  override def toString: String = {
    s"""<b>${this.fullname}</b>
       |${this.mailAddress}${roomAndBuilding(this)}${consultationhours(this)}
       |${this.consultationTimeComment match {
         case "" => ""
         case _  => "\n" + this.consultationTimeComment
       }}
       |""".stripMargin
  }

  private def consultationhours(lecturer: Lecturer): String = {
    val sprechzeiten
      : Boolean = lecturer.consultationDay != -1 && lecturer.consultationStartTime != -1 && lecturer.consultationEndTime != -1
    if (sprechzeiten) {
      val day = LocalDateTime.getWeekDay(lecturer.consultationDay)
      val startTime = LocalDateTime.prettyHourIntervall(lecturer.consultationStartTime)
      val endTime = LocalDateTime.prettyHourIntervall(lecturer.consultationEndTime)
      s"""|
         |
         |<b>Sprechzeiten:</b>
          |$day, $startTime - $endTime Uhr""".stripMargin
    } else ""
  }

  private def roomAndBuilding(lecturer: Lecturer): String = {
    if (lecturer.building != "" && lecturer.room != "") {
      s"""|
         |Raum: ${lecturer.building}${lecturer.room}""".stripMargin
    } else ""
  }

}
