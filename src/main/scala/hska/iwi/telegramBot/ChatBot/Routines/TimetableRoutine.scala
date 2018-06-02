package hska.iwi.telegramBot.ChatBot.Routines

import com.rivescript.RiveScript
import hska.iwi.telegramBot.news.Specialisation
import hska.iwi.telegramBot.service._
import hska.iwi.telegramBot.timetable.TimetableEntry
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

class TimetableRoutine extends CustomSubroutine with Instances {

  implicit val jsonDefaultFormats: DefaultFormats.type = org.json4s.DefaultFormats

  override def call(rs: RiveScript, args: Array[String]): String = {
    args.foreach(arg => logger.debug("Parameter bei TimetableRoutine" + arg))
    args.headOption match {
      case Some(param) => runWithParam(param, rs)
      case _           => requestTimetable(0, rs)
    }
  }

  private def runWithParam(param: String, rs: RiveScript): String = param.toLowerCase() match {
    case "gestern"                    => requestTimetable(LocalDateTime.getWeekDayPlusBonusDays(-1), rs)
    case "vorgestern"                 => requestTimetable(LocalDateTime.getWeekDayPlusBonusDays(-2), rs)
    case "morgen"                     => requestTimetable(LocalDateTime.getWeekDayPlusBonusDays(1), rs)
    case "übermorgen"                 => requestTimetable(LocalDateTime.getWeekDayPlusBonusDays(2), rs)
    case "heute"                      => requestTimetable(LocalDateTime.getWeekDayPlusBonusDays(0), rs)
    case "montags" | "montag"         => requestTimetable(1, rs)
    case "dienstags" | "dienstag"     => requestTimetable(2, rs)
    case "mittwochs" | "mittwoch"     => requestTimetable(3, rs)
    case "donnerstags" | "donnerstag" => requestTimetable(4, rs)
    case "freitags" | "freitag"       => requestTimetable(5, rs)
    case _ =>
      "Das letzte Wort kenne ich leider nicht. Möchtest du deinen Stundenplan für heute, morgen oder einen bestimmten Wochentag erfahren?"
  }

  def requestTimetable(dayOfWeek: Int, rs: RiveScript): String = {

    val studyConfig = redis.getStudySettingsForUser(UserID(rs.currentUser().toInt))

    if (studyConfig.isDefined) {

      val timetableURL = FeedURL.timetable + studyConfig.get.course.toString + "/" + Specialisation
        .getShortCutByName(studyConfig.get.specialisation) + "/" + studyConfig.get.semester.toString
      val content = HTTPGet.get(timetableURL)

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
    } else {
      "Ich weiß noch nicht, zu welchem Studiengang Du den Stundenplan sehen möchtest. In /settings kannst Du es auswählen."

    }

  }
}
