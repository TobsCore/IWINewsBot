package hska.iwi.telegramBot.commands

import java.io.{File, FileOutputStream}
import java.nio.file.{Files, Paths}

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding
import com.google.cloud.speech.v1.{
  RecognitionAudio,
  RecognitionConfig,
  SpeechClient,
  SpeechRecognitionResult
}
import com.google.protobuf.ByteString
import hska.iwi.telegramBot.ChatBot.ChatBot
import hska.iwi.telegramBot.ChatBot.Marker.ChatBotMarker
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.methods.{GetFile, ParseMode}
import info.mukel.telegrambot4s.models.Message
import org.gagravarr.opus.OpusFile

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

trait Chat extends Commands {
  _: TelegramBot =>

  private val speechClient: Try[SpeechClient] = Try(SpeechClient.create())

  onMessage { implicit msg =>
    using(_.text) { input =>
      if (!input.startsWith("/")) {
        chatbotAnswer(input)
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
                  val dirName = "./voices"
                  val fileName = s"$dirName/voice${voice.fileId}.oga"
                  // Create directory, if it doesn't exist already
                  val directory = new File(dirName)
                  if (!directory.exists) {
                    directory.mkdir
                  }
                  val stream = new FileOutputStream(fileName)
                  stream.write(bytes.toArray)

                  logger.debug(s"Datei mit ${bytes.size} Bytes erhalten.")
                  reply(
                    "<i>Der Text wird aus der Sprachdatei extrahiert. Dies kann einige Momente " +
                      "dauern...</i>",
                    parseMode = Some(ParseMode.HTML))
                  logger.debug(s"File URL: $url")
                  logger.debug(s"File: $fileName")
                  logger.debug(s"Mime-Type: ${voice.mimeType}")

                  speechClient match {
                    case Failure(exception) =>
                      logger.warn("Couldn't instatiate speech client.")
                      logger.debug(exception.getMessage)
                    case Success(_: SpeechClient) =>
                      logger.debug(s"Voice File: $fileName")
                      logger.debug("Recognizing speech from input")
                      val response = recognizeInput(fileName)
                      response match {
                        case Nil =>
                          logger.debug("No response.")
                          reply(
                            "Es konnte kein Text erkannt werden. Höre dir Deine Sprachnachricht " +
                              "doch noch einmal an, vielleicht ist dein Mikrofon verdeckt gewesen.")
                        case firstResponse :: _ =>
                          // There can be several alternative transcripts for a given chunk of
                          // speech. Just use the
                          // first (most likely) one here.
                          firstResponse.getAlternativesList.asScala.headOption match {
                            case None =>
                              logger.error(
                                "If there is a result, there must be alternative results. This " +
                                  "error message should therefore never appear and most likely is" +
                                  " caused by a JSON error.")
                            case Some(alternative) =>
                              // In thise case the voice text could be transcribed. This will
                              // trigger the chatbot and log some information.
                              val input = alternative.getTranscript
                              logger.info(s"Voice Transcription: $input")
                              reply(s"Ich habe verstanden: <i>$input</i>",
                                    parseMode = Some(ParseMode.HTML))
                              logger.info(ChatBotMarker(), s"Voice Command! - File $fileName")
                              chatbotAnswer(input)
                          }

                      }
                  }
                }
              case None =>
                logger.error("No file path was returned.")
            }
          case Failure(e) =>
            logger.error("Exception: " + e)
        }
      }
    }
  }

  private def chatbotAnswer(input: String)(implicit msg: Message) = {
    val user = msg.from.get
    // Is a chat message and not a command
    logger.trace(s"Received Chat Message: $input")
    logger.info(ChatBotMarker(), s"Input: [$input] by [$user]")
    val replyMessage = Chat.chatBot.reply(user.id.toString, input)
    logger.info(ChatBotMarker(), s"Output: [$replyMessage]")
    reply(replyMessage, parseMode = Some(ParseMode.HTML))
  }

  private def recognizeInput(fileName: String): Seq[SpeechRecognitionResult] = {
    val path = Paths.get(fileName)
    val data = Files.readAllBytes(path)
    val audioBytes: ByteString = ByteString.copyFrom(data)
    val opusFile = new OpusFile(new File(fileName))
    logger.debug(s"Frequencey: ${opusFile.getInfo.getRate} Hz")

    // Builds the sync recognize request
    val config = RecognitionConfig
      .newBuilder()
      .setEncoding(AudioEncoding.OGG_OPUS)
      .setSampleRateHertz(opusFile.getInfo.getRate.toInt)
      .setLanguageCode("de-De")
      .build()
    val audio = RecognitionAudio
      .newBuilder()
      .setContent(audioBytes)
      .build()

    speechClient.get.recognize(config, audio).getResultsList.asScala.toList
  }
}

object Chat {
  val chatBot = new ChatBot
}
