package hska.iwi.telegramBot.ChatBot.Routines

import com.rivescript.`macro`.Subroutine
import com.typesafe.scalalogging.Logger

abstract class CustomSubroutine extends Subroutine {
  val logger = Logger(getClass)
}
