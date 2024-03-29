package hska.iwi.telegramBot.ChatBot

import com.rivescript.{Config, RiveScript}
import com.typesafe.scalalogging.Logger

import scala.io.{Codec, Source}

class ChatBot extends RiveScript(Config.utf8()) {
  val logger = Logger(getClass)
  implicit val codec = Codec("UTF-8")

  /**
    * The rive files need to be read from the resources folder, since it is included in the jar
    * file and these files are needed for execution. In a previous version, these files were
    * stored in the projects root directory but wouldn't be exported. Reading those files should
    * be done by reading the input stream, since loading the files failed. See https://github
    * .com/typesafehub/activator-akka-stream-scala/issues/39 for a comment regarding the read
    * method.
    */
  private val folder = "chatbot"
  private val files = List("main.rive", "fun.rive", "synonyms.rive", "arrays.rive", "lectures.rive")

  for (file <- files) {
    val streamedFile = getClass.getClassLoader.getResourceAsStream(s"$folder/$file")
    stream(Source.fromInputStream(streamedFile).getLines.toArray)
  }
  sortReplies()

  // Add Subroutines here
  setSubroutine("no-comprendo", new Routines.NoComprendoRoutine)
  setSubroutine("example", new Routines.ExampleRoutine)
  setSubroutine("timetable", new Routines.TimetableRoutine)
  setSubroutine("mensa", new Routines.MensaRoutine)
  setSubroutine("profs", new Routines.ProfsRoutine)
  setSubroutine("spaceandtime", new Routines.SpaceAndTimeRoutine)
  setSubroutine("profslectures", new Routines.ProfsLecturesRoutine)
}
