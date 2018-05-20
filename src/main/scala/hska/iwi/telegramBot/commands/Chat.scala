package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.ChatBot.ChatBot
import hska.iwi.telegramBot.ChatBot.Marker.ChatBotMarker
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.methods.ParseMode

trait Chat extends Commands {
  _: TelegramBot =>

  onMessage { implicit msg =>
    using(_.text) { input =>
      if (!input.startsWith("/")) {
        val user = msg.from.get
        // Is a chat message and not a command
        logger.trace(s"Received Chat Message: $input")
        logger.info(ChatBotMarker(), s"Input: [$input] by [$user]")
        val replyMessage = Chat.chatBot.reply(user.id.toString, input)
        logger.info(ChatBotMarker(), s"Output: [$replyMessage]")
        reply(replyMessage, parseMode = Some(ParseMode.HTML))
      }
    }
  }
}

object Chat {
  val chatBot = new ChatBot
}
