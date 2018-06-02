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
    if (timetablesFormatted.isEmpty) {
      s"""
         |<b>Stundenplan</b>
         |An diesem Tag sind keine Veranstaltungen eingetragen.
     """.stripMargin
    } else {
      s"""
         |<b>Stundenplan</b>
         |$timetablesFormatted
     """.stripMargin
    }

  }

  def singleDayEntriesFormatted(timetables: Seq[SingleDayTimetable]): String = {
    val stringbuilder = new StringBuilder()
    for (timetable <- timetables) {
      stringbuilder.append(lectureEntries(timetable))
    }
    stringbuilder.toString
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
                                .prettyHourIntervall(entry.endTime)} Uhr""".stripMargin)
      if (entry.interval != "WEEKLY") {
        stringBuilder.append(s" (${intervalToGermanString(entry.interval)})")
      }
      stringBuilder.append(s"""
           |${entry.lectureName}
           |""".stripMargin)
      if (!entry.group.isEmpty) {
        stringBuilder.append(s"Gruppe ${entry.group}\n")
      }
      stringBuilder.append(s"""${lecturerNames(entry)}
           |${rooms(entry)}
           |""".stripMargin)
    }
    stringBuilder.toString
  }

  def rooms(entry: LectureEntry): String = {
    val stringBuilder = new StringBuilder
    val locations = entry.locations.filterNot(r => r.room.isEmpty && r.building.isEmpty)
    var size = locations.size
    for (room <- locations) {
      stringBuilder.append(room.building + room.room)
      if (size > 1) {
        stringBuilder.append(" / ")
        size -= 1
      }
    }
    stringBuilder.toString
  }

  def lecturerNames(entry: LectureEntry): String = {
    val stringBuilder = new StringBuilder
    val lectureNames = entry.lecturerNames.filterNot(_.isEmpty)
    var size = lectureNames.size
    for (lecturer <- lectureNames) {
      stringBuilder.append(lecturer)
      if (size > 1) {
        stringBuilder.append(" / ")
        size -= 1
      }
    }
    stringBuilder.toString
  }

  def intervalToGermanString(interval: String): String = interval match {
    case "SINGLE"      => "einmalig"
    case "BLOCK"       => "Blockveranstaltung"
    case "WEEKLY"      => "wöchentlich"
    case "FORTNIGHTLY" => "14-tägig"
    case _             => ""
  }

}
