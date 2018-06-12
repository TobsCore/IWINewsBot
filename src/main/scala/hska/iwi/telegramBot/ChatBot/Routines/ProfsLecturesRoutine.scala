package hska.iwi.telegramBot.ChatBot.Routines

import com.rivescript.RiveScript
import hska.iwi.telegramBot.lecturers.{Lecture, Lecturer}
import hska.iwi.telegramBot.service.{FeedURL, HTTPGet, Instances}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

class ProfsLecturesRoutine extends CustomSubroutine with Instances {

  implicit val jsonDefaultFormats: DefaultFormats.type = org.json4s.DefaultFormats

  override def call(rs: RiveScript, args: Array[String]): String = {
    args.foreach(arg => logger.debug("Parameter bei RoomFinderRoutine: " + arg))
    args.headOption match {
      case Some(param) => requestProfsLectures(param, rs)
      case _           => "Für welche Vorlesung möchtest du den Raum wissen?"
    }
  }

  def requestProfsLectures(param: String, rs: RiveScript): String = {
    logger.info(s"requested lecturers")
    logger.debug(s"param: $param")
    val stringBuilder = new StringBuilder
    val content: Option[String] = HTTPGet.get(FeedURL.profslecturesurl)
    logger.debug(s"content: $content")
    if (content.isDefined) {
      //parses the json entries
      val lecturerData: Option[Seq[Lecturer]] = Some(JsonMethods.parse(content.get).extract[Seq[Lecturer]].sortBy(_.lastname))
      logger.debug(s"Received ${lecturerData.get.size} Lecturers.")
      if (lecturerData.isDefined) {
        //selektiere Dozent
        val selectedLecturer = lecturerData.get.find(lec => lec.lastname.toLowerCase == param.toLowerCase)
        val lectures: Seq[Lecture] = selectedLecturer.get.lectures
        //Vorlesungsstring bauen
        lectures.map(_.longName).distinct.sorted.foreach(stringBuilder.append(_).append("\n"))
        val ausgabe = stringBuilder.toString()
        //Ausgabe
        s"""<b>${selectedLecturer.get.shortenedFullname}</b> unterrichtet in folgenden Vorlesungen:
           |$ausgabe
          """.stripMargin
      }else {
        logger.error("Couldn't parse the Json.")
        "Fehler beim Bereitstellen der Daten"
      }
    } else {
      logger.error("Couldn't connect to the server.")
      "Fehler beim Bereitstellen der Daten"
    } //end else
  } //end requestProfsLectures
}
