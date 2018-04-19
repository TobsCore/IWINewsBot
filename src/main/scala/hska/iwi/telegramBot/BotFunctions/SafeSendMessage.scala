package hska.iwi.telegramBot.BotFunctions

import info.mukel.telegrambot4s.api.{TelegramApiException, TelegramBot}
import info.mukel.telegrambot4s.methods.{ParseMode, SendMessage}
import info.mukel.telegrambot4s.models.ChatId

import scala.util.Failure

trait SafeSendMessage extends TelegramBot {

  def trySendMessage(chatID: ChatId, content: String): Unit = {
    request(SendMessage(chatID, content, parseMode = Some(ParseMode.HTML)))
      .onComplete {
        case Failure(telegramException: TelegramApiException) =>
          telegramException.errorCode match {
            case 400 =>
              logger.error(
                s"Received a 400 error [Bad Request] while trying to send message " +
                  s"to user with $chatID. Message: ${telegramException.message}")
            case 439 =>
              logger.error(
                s"Received a 439 error [Too many requests] while trying to send message " +
                  s"to user with $chatID")
            case 403 =>
              logger.error(
                s"Blocked by user: User with id $chatID has forbidden access, which " +
                  s"caused an error. The message could not be sent.")
            case e =>
              logger.error(s"Unknown error occured, with error-code $e. Better look into this.")
          }
        case _ => logger.debug(s"Sent message to user $chatID")
      }
  }
}
