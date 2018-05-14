package hska.iwi.telegramBot.BotFunctions

import akka.stream.BufferOverflowException
import scala.concurrent.duration._
import info.mukel.telegrambot4s.api.{TelegramApiException, TelegramBot}
import info.mukel.telegrambot4s.methods.{ParseMode, SendMessage}
import info.mukel.telegrambot4s.models.{ChatId, Message}

import scala.util.{Failure, Success}
import scala.language.postfixOps

trait SafeSendMessage extends TelegramBot {

  def trySendMessage(chatID: ChatId, content: String, attempts: Int = 0): Unit = {
    request(SendMessage(chatID, content, parseMode = Some(ParseMode.HTML)))
      .onComplete {
        case Failure(telegramException: TelegramApiException) =>
          telegramException.errorCode match {
            case 429 =>
              logger.error(
                s"Received a 429 error [Too many requests] while trying to send message " +
                  s"to user with $chatID")
            case 403 =>
              logger.error(
                s"Blocked by user: User with id $chatID has forbidden access, which " +
                  s"caused an error. The message could not be sent.")
            case e =>
              logger.error(s"Unknown error occured, with error-code $e. Better look into this.")
          }
        case Failure(exception: Throwable) =>
          exception match {
            case BufferOverflowException(_) =>
              logger.error(s"BufferOverflowException occured. Retry sending the message")
              Thread.sleep((2 seconds).toMillis)
              logger.info(s"Retrying to send message again. Attempt: ${attempts + 1}");
              trySendMessage(chatID, content, attempts + 1);
            case _ =>
              logger.error(
                s"Couldn't send message to user $chatID. Exception: ${exception.getMessage}.")
              logger.debug(exception.toString)
              logger.debug(exception.getStackTrace.toString)
          }
        case Success(msg: Message) =>
          logger.debug(s"Sent message with ID ${msg.messageId} to user $chatID")
      }
  }
}
