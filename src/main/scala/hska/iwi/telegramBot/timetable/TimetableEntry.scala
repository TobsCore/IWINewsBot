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
       |<b>Stundenplan</b>
       |$timetablesFormatted
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
    var firstElement = true

    for (entry <- entries) {
      val day = LocalDateTime.getWeekDay(entry.day)
      if (firstElement) {
        stringBuilder.append(s"\n<b>$day</b>")
        firstElement = false
      }
      stringBuilder.append(s"""
           |${LocalDateTime.prettyHourIntervall(entry.startTime)}-${LocalDateTime
                                .prettyHourIntervall(entry.endTime)} Uhr
           |${entry.lectureName}
           |${lecturerNames(entry)}
           |${rooms(entry)}
           |""".stripMargin)
    }
    stringBuilder.toString()
  }

  def rooms(entry: LectureEntry): String = {
    val stringBuilder = new StringBuilder
    var size = entry.locations.size
    for (room <- entry.locations) {
      stringBuilder.append(room.building + room.room)
      if (size > 1) {
        stringBuilder.append(" / ")
        size -= 1
      }
    }
    stringBuilder.toString()
  }

  def lecturerNames(entry: LectureEntry): String = {
    val stringBuilder = new StringBuilder
    var size = entry.lecturerNames.size
    for (lecturer <- entry.lecturerNames) {
      stringBuilder.append(lecturer)
      if (size > 1) {
        stringBuilder.append(" / ")
        size -= 1
      }
    }
    stringBuilder.toString()
  }

}
