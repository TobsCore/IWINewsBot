package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.ChatBot.ChatBot
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.methods.ParseMode

trait Chat extends Commands {
  _: TelegramBot =>

  val chatBot = new ChatBot

  onMessage { implicit msg =>
    using(_.text) { input =>
      if (!input.startsWith("/")) {
        // Is a chat message and not a command
        logger.trace(s"Received Chat Message: $input")
        reply(chatBot.reply(msg.from.get.id.toString, input), parseMode = Some(ParseMode.HTML))
      }
    }
  }

}
