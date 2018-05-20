package hska.iwi.telegramBot.ChatBot.Routines

import com.rivescript.RiveScript
import com.rivescript.`macro`.Subroutine
import com.typesafe.scalalogging.Logger
import hska.iwi.telegramBot.ChatBot.Marker.ChatBotMarker

/**
  * This Routine is used to handle chatbot functionality, if nothing can be matched. Rivescript
  * does support the star operator, which maps to anything, but in order to log requests
  * correctly, this routine is used.
  */
class NoComprendoRoutine extends Subroutine {
  val logger = Logger(getClass)

  val responses = List(
    "Das habe ich leider nicht verstanden.",
    "Ich weiß nicht, was du meinst. Eine Befehlsübersicht bekommst du über /help",
    "Da kann ich dir nicht helfen, sorry.",
  )

  override def call(rs: RiveScript, args: Array[String]): String = {
    logger.warn(ChatBotMarker(), s"No comprendo!")
    responses(scala.util.Random.nextInt(responses.size))
  }

}
