package hska.iwi.telegramBot.ChatBot.Routines

import com.rivescript.RiveScript
import hska.iwi.telegramBot.rooms.Room
import hska.iwi.telegramBot.service._
import hska.iwi.telegramBot.timetable.{LectureEntry, TimetableEntry}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import scala.collection.mutable

class RoomFinderRoutine extends CustomSubroutine with Instances {

  implicit val jsonDefaultFormats: DefaultFormats.type = org.json4s.DefaultFormats

  var lectures: Option[Seq[LectureEntry]] = None

  override def call(rs: RiveScript, args: Array[String]): String = {
    args.foreach(arg => logger.debug("Parameter bei RoomFinderRoutine: " + arg))
    args.headOption match {
      case Some(param) => requestLocalisation(param, rs)
      case _           => "Für welche Vorlesung möchtest du den Raum wissen?"
    }
  }

  def requestLocalisation(param: String, rs: RiveScript): String = {
    logger.info(s"requested localisation")
    logger.info(s"param: $param")
    val lectureLocationMap: Option[Map[String, Seq[Room]]] = JsonParser.myDecider("alltimetables")
    //val contentBlockcourses: mutable.Map[String, Seq[Room]] = JsonParser.myDecider("blockcourses")

    if (lectureLocationMap.isDefined) {
      logger.info(s"Received $lectureLocationMap")
      //gewünschtes Key-Value-Paar selektieren
      val selectedLecture = lectureLocationMap.get.find(_._1.toLowerCase == param.toLowerCase.replace("_", " "))

      //Ausgabe machen
      if (selectedLecture.isDefined) {
        val stringBuilder = new StringBuilder
        val locations = selectedLecture.get._2.filterNot(r => r.room.isEmpty && r.building.isEmpty)
        var size = locations.size
        var raum: String = ""
        if (size > 1) {
          raum = "in den folgenden Räumen"
        } else {
          raum = "im folgenden Raum"
        }
        for (room <- locations) {
          stringBuilder.append(room.building + room.room)
          if (size > 1) {
            stringBuilder.append(" / ")
            size -= 1
          }
        }
        val locationsausgabe = stringBuilder.toString
        s"""Die Vorlesung <b>${selectedLecture.get._1.toString}</b> findet $raum statt:
           |$locationsausgabe
         """.stripMargin
      } else {
        "Diese Vorlesung kenne ich nicht. Bitte prüfe die Schreibweise und probiere es erneut."
      }
    } else {
      logger.error("Couldn't connect to the server.")
      "Fehler beim Bereitstellen der Daten"
    }
  }
}
