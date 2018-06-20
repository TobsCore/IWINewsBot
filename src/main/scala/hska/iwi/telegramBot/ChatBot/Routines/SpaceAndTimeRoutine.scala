package hska.iwi.telegramBot.ChatBot.Routines

import com.rivescript.RiveScript
import hska.iwi.telegramBot.rooms.Room
import hska.iwi.telegramBot.service._
import hska.iwi.telegramBot.timetable.{LectureEntry, TimetableEntry}
import org.json4s.DefaultFormats

class SpaceAndTimeRoutine extends CustomSubroutine with Instances {

  implicit val jsonDefaultFormats: DefaultFormats.type = org.json4s.DefaultFormats

  var lectures: Option[Seq[LectureEntry]] = None

  override def call(rs: RiveScript, args: Array[String]): String = {
    //Getrennte param zusammenfügen
    val stringArgs: String = args.mkString(" ")
    logger.debug("Parameter bei RoomFinderRoutine: " + stringArgs)
    args.headOption match {
      case Some(_) => requestLocalisation(stringArgs, rs)
      case _       => "Für welche Vorlesung möchtest du den Raum wissen?"
    }
  }

  def requestLocalisation(param: String, rs: RiveScript): String = {
    logger.debug(s"requested localisation")
    logger.debug(s"param: $param")
    val lectureLocationMap: Option[Map[String, Map[String, Seq[Room]]]] =
      JsonParser.myDecider("alltimetables")
    val blockCoursesMap: Option[Map[String, Map[String, Seq[Room]]]] =
      JsonParser.myDecider("blockcourses")

    if (lectureLocationMap.isDefined || blockCoursesMap.isDefined) {
      logger.debug(s"Received $lectureLocationMap")
      //gewünschtes Key-Value-Paar selektieren
      val selectedLectureTimetable =
        lectureLocationMap.get.find(_._1.toLowerCase == param.toLowerCase.replace("_", " "))
      val selectedLectureBlockCourse =
        blockCoursesMap.get.find(_._1.toLowerCase == param.toLowerCase.replace("_", " "))
      if (selectedLectureTimetable.isDefined) {
        ausgabe(selectedLectureTimetable)
      } else if (selectedLectureBlockCourse.isDefined) {
        ausgabe(selectedLectureBlockCourse)
      } else {
        logger.debug(s"Could not find information about $param.")
        s"Ich kenne die Veranstaltung $param nicht."
      }
    } else {
      logger.error("Couldn't connect to the server.")
      "Fehler beim Bereitstellen der Daten"
    }
  } //end def

  def ausgabe(map: Option[(String, Map[String, Seq[Room]])]): String = {
    val mapstringBuilder = new StringBuilder()
    var size: Int = 0
    var raum: String = ""
    //über Einträge der Datum-Raum-Map iterieren
    for (zweiteMap: (String, Seq[Room]) <- map.get._2) {
      val locationstringBuilder = new StringBuilder
      val locations = zweiteMap._2.filterNot(r => r.room.isEmpty && r.building.isEmpty)
      val date = zweiteMap._1
      size = locations.size
      raum = if (size > 1) {
        "in den folgenden Räumen"
      } else {
        "im folgenden Raum"
      }
      for (location <- locations) {
        locationstringBuilder.append(location.building + location.room)
        if (size > 1) {
          locationstringBuilder.append(" / ")
          size -= 1
        }
      }
      mapstringBuilder.append(date + s" $raum: " + locationstringBuilder.toString() + "\n")
    }
    //Unterscheidung ein Raum/mehrere Räume

    val locationsausgabe = mapstringBuilder.toString
    s"""Hier sind die Infos zur Vorlesung <b>${map.get._1}</b>:
       |$locationsausgabe
     """.stripMargin
  }
}
