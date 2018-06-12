package hska.iwi.telegramBot.ChatBot.Routines

import com.rivescript.RiveScript
import hska.iwi.telegramBot.lecturers.Lecturer
import hska.iwi.telegramBot.service.{FeedURL, HTTPGet, Instances}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

class ProfsRoutine extends CustomSubroutine with Instances {

  implicit val jsonDefaultFormats: DefaultFormats.type = org.json4s.DefaultFormats

  var lecturers: Option[Seq[Lecturer]] = None
  var result: String = ""

  override def call(rs: RiveScript, args: Array[String]): String = {
    args.headOption match {
      case Some(param) => callProfsWithParam(param, rs)
      case _ => "Zu welchem Professor möchtest du etwas erfahren?"
    }
  }

  private def callProfsWithParam(param: String, rs: RiveScript): String = {
    //Daten abholen
    logger.info(s"requested lecturers")
    logger.debug(s"param: $param")
    val content = HTTPGet.get(FeedURL.lecturer)
    if (content.isDefined) {
      //parses the json entries and stores them in a lecturers object
      lecturers = Some(JsonMethods.parse(content.get).extract[Seq[Lecturer]].sortBy(_.lastname))

      logger.debug(s"Received ${lecturers.get.size} Lecturers.")
      //Sequence auf Wert prüfen
      if (lecturers.isDefined) {
        val selectedLecturer = lecturers.get.find(lec => lec.lastname.toLowerCase == param.toLowerCase)
        if (selectedLecturer.isDefined) {
          //Ausgabe machen
          s"Hier sind die Informationen: ${selectedLecturer.get.toString}"
        }
        else {
          "Diesen Professor/diese Professorin kenne ich nicht. Bitte prüfe die Schreibweise und probiere es erneut."
        }
      } else {
        logger.error("Couldn't parse the Json.")
        "Fehler beim Bereitstellen der Daten"
      }
    } else {
      logger.error("Couldn't connect to the server.")
      "Fehler beim Bereitstellen der Daten"
    }
  }
}
