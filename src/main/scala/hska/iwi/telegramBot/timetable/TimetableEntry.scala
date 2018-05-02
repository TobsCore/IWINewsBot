package hska.iwi.telegramBot.timetable

import com.typesafe.scalalogging.Logger
import hska.iwi.telegramBot.service.LocalDateTime

case class TimetableEntry(courseOfStudies: String,
                          idSemester: Int,
                          moduleSpecialization: Int,
                          semester: Int,
                          semesterName: String,
                          timetables: Seq[SingleDayTimetable]) {

  val logger = Logger(getClass)
  override def toString: String = {
    val timetablesFormatted = singleDayEntriesFormatted(timetables)

    s"""
       |Stundenplan
       |
       |$timetablesFormatted
       |
     """.stripMargin

  }

  def singleDayEntriesFormatted(timetables: Seq[SingleDayTimetable]): String = {
    val stringbuilder = new StringBuilder()
    for (timetable <- timetables) {
      stringbuilder.append(lectureEntries(timetable))
    }
    stringbuilder.toString()
  }

  def lectureEntries(singleDayTimetable: SingleDayTimetable): String = {
    val entries = singleDayTimetable.entries
    val stringBuilder = new StringBuilder

    /*    stringBuilder.append(s"""
         |${LocalDateTime.getWeekDay(entries(1).day)}
         |
       """.stripMargin)*/

    for (entry <- entries) {
      stringBuilder.append(s"""
           |
           |${entry.lectureName}
           |${LocalDateTime.prettyHourIntervall(entry.startTime)}-${LocalDateTime
                                .prettyHourIntervall(entry.endTime)}
           |
         """.stripMargin)
    }
    stringBuilder.toString()
  }

}
