package hska.iwi.telegramBot.ChatBot

import com.rivescript.{Config, RiveScript}
import com.typesafe.scalalogging.Logger

import scala.io.Source

class ChatBot extends RiveScript(Config.utf8()) {
  val logger = Logger(getClass)

  /**
    * The rive files need to be read from the resources folder, since it is included in the jar
    * file and these files are needed for execution. In a previous version, these files were
    * stored in the projects root directory but wouldn't be exported. Reading those files should
    * be done by reading the input stream, since loading the files failed. See https://github
    * .com/typesafehub/activator-akka-stream-scala/issues/39 for a comment regarding the read
    * method.
    */
  private val folder = "chatbot"
  private val mainStream = getClass.getClassLoader.getResourceAsStream(s"$folder/main.rive")
  private val synonyms = getClass.getClassLoader.getResourceAsStream(s"$folder/synonyms.rive")
  stream(Source.fromInputStream(mainStream).getLines.toArray)
  stream(Source.fromInputStream(synonyms).getLines.toArray)
  sortReplies()

  // Add Subroutines here
  setSubroutine("no-comprendo", new Routines.NoComprendoRoutine)
  setSubroutine("example", new Routines.ExampleRoutine)
  setSubroutine("timetable", new Routines.TimetableRoutine)
  setSubroutine("mensa", new Routines.MensaRoutine)
  setSubroutine("profs", new Routines.ProfsRoutine)
}
