package hska.iwi.telegramBot.ChatBot

import com.rivescript.{Config, RiveScript}
import com.typesafe.scalalogging.Logger

class ChatBot extends RiveScript(Config.utf8()) {
  loadFile("./telegramChatBotMain.rive")
  sortReplies()
  val logger = Logger(getClass)

  // Add Subroutines here
  setSubroutine("example", new Routines.ExampleRoutine)
  setSubroutine("timetable", new Routines.TimetableRoutine)
  setSubroutine("mensa", new Routines.MensaRoutine)
}
