package hska.iwi.telegramBot.ChatBot

import com.rivescript.{Config, RiveScript}

class ChatBot extends RiveScript(Config.utf8()) {
  loadFile("./telegramChatBotMain.rive")
  sortReplies()

  // Add Subroutines here
  setSubroutine("no-comprendo", new Routines.NoComprendoRoutine)
  setSubroutine("example", new Routines.ExampleRoutine)
  setSubroutine("timetable", new Routines.TimetableRoutine)
  setSubroutine("mensa", new Routines.MensaRoutine)
}
