package hska.iwi.telegramBot.commands

import java.io.FileOutputStream
import java.nio.file.{Files, Paths}

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding
import com.google.cloud.speech.v1.{RecognitionAudio, RecognitionConfig, SpeechClient}
import com.google.protobuf.ByteString
import hska.iwi.telegramBot.ChatBot.ChatBot
import hska.iwi.telegramBot.ChatBot.Marker.ChatBotMarker
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.methods.ParseMode
import info.mukel.telegrambot4s.methods.GetFile

import scala.util.{Failure, Success, Try}
import scala.collection.JavaConverters._

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
                  bytes <- Unmarshal(res).to[akka.util.ByteString]
                } /* do */ {
                  new FileOutputStream(s"./Voice${voice.fileId}.ogg").write(bytes.toArray)
                  logger.debug(s"Datei mit ${bytes.size} Bytes erhalten.")
                  reply(
                    "<i>Der Text wird aus der Sprachdatei extrahiert. Dies kann einige Momente dauern...</i>",
                    parseMode = Some(ParseMode.HTML))
                  val encoding = voice.mimeType
                  logger.debug(s"File URL: $url")
                  logger.debug(s"Mime-Type: $encoding")

                  Try(SpeechClient.create()) match {
                    case Failure(exception) =>
                      logger.warn("Couldn't instatiate speech client.")
                      logger.debug(exception.getMessage)
                    case Success(speechClient: SpeechClient) =>
                      logger.info("Successfully created speech client")
                      // The path to the audio file to transcribe
                      val fileName = s"./Voice${voice.fileId}.ogg"

                      // Reads the audio file into memory
                      val path = Paths.get(fileName)
                      val data = Files.readAllBytes(path)
                      val audioBytes = ByteString.copyFrom(data)

                      // Builds the sync recognize request
                      val config = RecognitionConfig
                        .newBuilder()
                        .setEncoding(AudioEncoding.OGG_OPUS)
                        .setSampleRateHertz(48000)
                        .setLanguageCode("de-De")
                        .build()
                      val audio = RecognitionAudio
                        .newBuilder()
                        .setContent(audioBytes)
                        .build()

                      val response = speechClient.recognize(config, audio)
                      val results = response.getResultsList.asScala
                      results.foreach { result =>
                        // There can be several alternative transcripts for a given chunk of speech. Just use the
                        // first (most likely) one here.
                        val alternative = result.getAlternativesList.asScala.headOption
                        if (alternative.isDefined) {
                          val input = alternative.get.getTranscript
                          logger.debug(s"Voice File: $fileName")
                          logger.info(s"Voice Transcription: $input")

                          val user = msg.from.get.id
                          reply(s"Ich habe verstanden: <i>$input</i>",
                                parseMode = Some(ParseMode.HTML))
                          logger.info(ChatBotMarker(), s"Voice Command! - File $fileName")
                          logger.info(ChatBotMarker(), s"Input: [$input] by [$user]")
                          val replyMessage =
                            Chat.chatBot.reply(user.toString, input)
                          logger.info(ChatBotMarker(), s"Output: [$replyMessage]")
                          reply(replyMessage, parseMode = Some(ParseMode.HTML))
                        }
                      }
                  }
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
