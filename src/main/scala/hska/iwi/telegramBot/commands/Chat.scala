package hska.iwi.telegramBot.commands

import java.io.FileOutputStream

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.util.ByteString
import hska.iwi.telegramBot.ChatBot.ChatBot
import hska.iwi.telegramBot.ChatBot.Marker.ChatBotMarker
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.methods.ParseMode
import info.mukel.telegrambot4s.methods.GetFile

import scala.util.{Failure, Success}

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

  onMessage { implicit msg =>
    using(_.voice) { voice =>
      if (voice.duration > 7) {
        reply("Die Sprachnachricht darf nicht länger als 7 Sekunden sein.")
      } else {
        request(GetFile(voice.fileId)).onComplete {
          case Success(file) =>
            file.filePath match {

              case Some(filePath) =>
                val url = s"https://api.telegram.org/file/bot$token/$filePath"
                for {
                  res <- Http().singleRequest(HttpRequest(uri = Uri(url)))
                  if res.status.isSuccess()
                  bytes <- Unmarshal(res).to[ByteString]
                } /* do */ {
                  new FileOutputStream(s"./Voice${voice.fileId}.ogg").write(bytes.toArray)
                  reply(s"Datei mit ${bytes.size} Bytes erhalten.")
                }
              case None =>
                logger.error("No file path was returned")
            }
          case Failure(e) =>
            logger.error("Exception: " + e)
        }
      }
    }
  }
}

object Chat {
  val chatBot = new ChatBot
}
