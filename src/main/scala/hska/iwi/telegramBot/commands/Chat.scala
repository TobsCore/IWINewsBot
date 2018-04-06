package hska.iwi.telegramBot.commands

import com.rivescript.{Config, RiveScript}
import hska.iwi.telegramBot.ChatBot.Routines.ExampleRoutine
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands

trait Chat extends Commands {
  _: TelegramBot =>

  val chatBot = new RiveScript(Config.utf8())
  chatBot.loadFile("./telegramChatBotMain.rive")
  chatBot.sortReplies()
  chatBot.setSubroutine("example", new ExampleRoutine)

  onMessage { implicit msg =>
    using(_.text) { input =>
      if (!input.startsWith("/")) {
        // Is a chat message and not a command
        logger.trace(s"Received Chat Message: $input")
        reply(chatBot.reply(msg.from.get.id.toString, input))
      }
    }
  }

}
