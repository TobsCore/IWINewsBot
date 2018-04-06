package hska.iwi.telegramBot.commands

import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands

trait Chat extends Commands {
  _: TelegramBot =>

  onMessage { implicit msg =>
    using(_.text) { input =>
      if (!input.startsWith("/")) {
        // Is a chat message and not a command
        logger.trace(s"Received Chat Message: $input")
        reply(s"Received Chat Message: $input")

        // TODO: Pass input to RiveScript and reply with answer
      }
    }
  }

}
