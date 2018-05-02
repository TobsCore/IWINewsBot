package hska.iwi.telegramBot.ChatBot.Routines

import com.rivescript.RiveScript
import com.rivescript.`macro`.Subroutine
import hska.iwi.telegramBot.service.{FeedURL, HTTPGet, LocalDateTime}
import hska.iwi.telegramBot.timetable.TimetableEntry
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

class TimetableRoutine extends Subroutine {

  implicit val jsonDefaultFormats: DefaultFormats.type = org.json4s.DefaultFormats

  override def call(rs: RiveScript, args: Array[String]): String = {
    args.headOption match {
      case Some(param) => runWithParam(param, rs)
      case _           => requestTimetable(0)
    }
  }

  private def runWithParam(param: String, rs: RiveScript): String = param.toLowerCase() match {
    case "morgen"                     => requestTimetable(LocalDateTime.getWeekDayPlusBonusDays(1))
    case "heute"                      => requestTimetable(LocalDateTime.getWeekDayPlusBonusDays(0))
    case "montags" | "montag"         => requestTimetable(1)
    case "dienstags" | "dienstag"     => requestTimetable(2)
    case "mittwochs" | "mittwoch"     => requestTimetable(3)
    case "donnerstags" | "donnerstag" => requestTimetable(4)
    case "freitags" | "freitag"       => requestTimetable(5)
    case _                            => requestTimetable(0)
  }

  def requestTimetable(dayOfWeek: Int): String = {

    val content = HTTPGet.get(FeedURL.timetable + "INFM" + "/" + "1" + "/" + "2")

    if (content.isDefined) {

      val timetable: Option[TimetableEntry] = Some(
        JsonMethods.parse(content.get).extract[TimetableEntry])

      val timetableContent: Option[TimetableEntry] = dayOfWeek match {
        case 0 => timetable
        case _ =>
          val temp = timetable.get.timetables(dayOfWeek - 1)
          Some(
            TimetableEntry(
              timetable.get.courseOfStudies,
              timetable.get.idSemester,
              timetable.get.moduleSpecialization,
              timetable.get.semester,
              timetable.get.semesterName,
              Seq(temp)
            ))
      }

      timetableContent.get.toString

    } else {
      ""
    }
  }

}
