package hska.iwi.telegramBot.service

import com.typesafe.scalalogging.Logger
import hska.iwi.telegramBot.rooms.Room
import hska.iwi.telegramBot.timetable._
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import scala.collection.mutable

object JsonParser {

  val logger = Logger(getClass)
  implicit val jsonDefaultFormats: DefaultFormats.type = org.json4s.DefaultFormats
  
  def myDecider(urlName: String): Option[Map[String, Seq[Room]]] = urlName match
  {
    case "alltimetables" => myallTimetablesParser(FeedURL.alltimetables)
    //case "blockcourses"  => myBlockCoursesParser(FeedURL.blockCourses)
    case _               => None
  }

  def myallTimetablesParser(url: String): Option[Map[String, Seq[Room]]] = {
    //Map anlegen
    val lectureLocationMap: mutable.Map[String, Seq[Room]] = mutable.Map.empty
    //URL abholen/bauen
    val content: Option[String] = HTTPGet.get(url)
    //logger.debug(s"content: $content")
    if (content.isDefined) {
      //Json abholen und extracten
      val timetableentries = Some(
        JsonMethods.parse(content.get).extract[Seq[TimetableEntry]])
      //logger.debug(s"timetableentries: $timetableentries")

      if (timetableentries.isDefined) {
        //über Seq[TimetableEntry] iterieren
        for (timetables: Seq[TimetableEntry] <- timetableentries) {
          for (timetable <- timetables) {
            for (singletimetable <- timetable.timetables) {
              for (entry <- singletimetable.entries) {
                val roomSeq: Seq[Room] = entry.locations
                val lectureName = entry.lectureName
                //Map ergänzen/füllen
                //logger.debug(s"entry: $entry")
                lectureLocationMap += (lectureName -> roomSeq)
              }
            }
          }
        } //end for
      }
      Some(lectureLocationMap.toMap)
    } else {
      None
    }
  }

  //def myBlockCoursesParser(url: String): String = {   //MUSS NOCH AUF BLOCKCOURSES ANGEPASST WERDEN!!!!!!!!!!
    //val content: Option[String] = HTTPGet.get(url)

    //if (content.isDefined) {

     // val timetable: Option[Seq[TimetableEntry]] = Some(
      //  JsonMethods.parse(content.get).extract[Seq[TimetableEntry]])
      //timetable.get.toString()
    //} else {
     // ""
    //}
  //}
}
