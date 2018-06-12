package hska.iwi.telegramBot.service

import com.typesafe.scalalogging.Logger
import hska.iwi.telegramBot.rooms.Room
import hska.iwi.telegramBot.timetable._
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods
import java.text.SimpleDateFormat

import scala.collection.immutable.ListMap
import scala.collection.mutable

object JsonParser {

  val logger = Logger(getClass)
  implicit val jsonDefaultFormats: DefaultFormats.type = org.json4s.DefaultFormats
  
  def myDecider(urlName: String): Option[Map[String, Map[String, Seq[Room]]]] = urlName match
  {
    case "alltimetables" => myallTimetablesParser(FeedURL.alltimetables)
    case "blockcourses"  => myBlockCoursesParser(FeedURL.blockCourses)
    case _               => None
  }

  def myallTimetablesParser(url: String): Option[Map[String, Map[String, Seq[Room]]]] = {
    //Map anlegen
    val lectureLocationMap: mutable.Map[String, Map[String, Seq[Room]]] = mutable.Map.empty
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
                val dateRoomMap: mutable.Map[String, Seq[Room]] = mutable.Map.empty
                val roomSeq: Seq[Room] = entry.locations
                val lectureName = entry.lectureName
                val day: String = LocalDateTime.getWeekDay(entry.day).toString
                //Datum-String bauen
                val myDate: String = s"${intervalToGermanString(entry.interval)} am $day von ${LocalDateTime.prettyHourIntervall(entry.startTime)}-${LocalDateTime
                  .prettyHourIntervall(entry.endTime)} Uhr"
                dateRoomMap += (myDate -> roomSeq)
                lectureLocationMap += (lectureName -> dateRoomMap.toMap)
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

  def myBlockCoursesParser(url: String): Option[Map[String, Map[String, Seq[Room]]]] = {
    //Map anlegen
    val lectureLocationMap: mutable.Map[String, Map[String, Seq[Room]]] = mutable.Map.empty
    var singleDateMap: Map[String, Seq[Room]] = Map.empty
    //URL abholen/bauen
    val content: Option[String] = HTTPGet.get(url)
    //logger.debug(s"content: $content")
    if (content.isDefined) {
      //Json abholen und extracten
      val blockcourseentry = Some(
        JsonMethods.parse(content.get).extract[Seq[BlockCourseEntry]])
      //logger.debug(s"timetableentries: $timetableentries")

      if (blockcourseentry.isDefined) {
        //über Seq[TimetableEntry] iterieren
        for (entries <- blockcourseentry) {
          for (entry <- entries) {
            val lectureName: String = entry.lectureName
            val singleDates: Seq[BlockCourseSingleDate] = entry.singleDates
            singleDateMap = mapBuilderBlockCourses(singleDates)
            lectureLocationMap += (lectureName -> singleDateMap)
          }
        } //end for
      }
      Some(lectureLocationMap.toMap)
    } else {
      None
    }
  } //end myBlockCoursesParser

  def mapBuilderBlockCourses(blockCourseSingleDate: Seq[BlockCourseSingleDate]): Map[String, Seq[Room]] = {
    val singleDateMap: mutable.Map[String, Seq[Room]] = mutable.Map.empty
    //über die einzelnen SingleDate iterieren und Ausgabe bauen
    for (singleDate <- blockCourseSingleDate) {
      val roomSeq: Seq[Room] = singleDate.locations
      val inputFormat = new SimpleDateFormat("yyyy-MM-dd")
      val outputFormat = new SimpleDateFormat("dd.MM.yyyy")
      val formattedDate = outputFormat.format(inputFormat.parse(singleDate.date))
      val myDateString: String = s"Am $formattedDate von ${LocalDateTime.prettyHourIntervall(singleDate.startTime)}-${LocalDateTime
        .prettyHourIntervall(singleDate.endTime)} Uhr"
      singleDateMap += (myDateString -> roomSeq)
    }
    //singleDateMap.toMap
    ListMap(singleDateMap.toMap.toSeq.sortBy(_._1):_*)
  }

  def intervalToGermanString(interval: String): String = interval match {
    case "SINGLE"      => "Einmalig"
    case "BLOCK"       => "Blockveranstaltung"
    case "WEEKLY"      => "Wöchentlich"
    case "FORTNIGHTLY" => "14-tägig"
    case _             => ""
  } //end intervalToGermanString
}
