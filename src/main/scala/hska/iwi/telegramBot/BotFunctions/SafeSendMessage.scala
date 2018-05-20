package hska.iwi.telegramBot.BotFunctions

import akka.stream.BufferOverflowException
import hska.iwi.telegramBot.service.{Instances, UserID}
import info.mukel.telegrambot4s.api.{TelegramApiException, TelegramBot}
import info.mukel.telegrambot4s.methods.{ParseMode, SendMessage}
import info.mukel.telegrambot4s.models.{ChatId, Message}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

trait SafeSendMessage extends TelegramBot with Instances {

  def trySendMessage(chatID: ChatId, content: String, attempts: Int = 0): Unit = {
    request(SendMessage(chatID, content, parseMode = Some(ParseMode.HTML)))
      .onComplete {
        case Failure(telegramException: TelegramApiException) =>
          telegramException.errorCode match {
            case 429 =>
              logger.warn(
                s"Received a 429 error [Too many requests] while trying to send message " +
                  s"to user with $chatID")
              logger.info(
                s"Retrying to send message again to user with id $chatID. Attempt: ${attempts + 1}")
              trySendMessage(chatID, content, attempts + 1);
            case 403 =>
              logger.warn(
                s"Blocked by user: User with id $chatID has forbidden access, which " +
                  s"caused an error. The message could not be sent.")
              if (chatID.isChat) {
                logger.info(s"User $chatID will be removed from database.")
                val user = UserID(chatID.toEither.left.get.toInt)
                redis.removeUser(user)
              }
            case e =>
              logger.error(s"Unknown error occured, with error-code $e. Better look into this.")
          }
        case Failure(exception: Throwable) =>
          exception match {
            case BufferOverflowException(_) =>
              logger.warn(s"BufferOverflowException occured. Retry sending the message")
              Thread.sleep((2 seconds).toMillis)
              logger.info(
                s"Retrying to send message again to user with id $chatID. Attempt: ${attempts + 1}")
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
