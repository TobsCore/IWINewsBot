package hska.iwi.telegramBot.service

import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.methods.{ParseMode, SendMessage}
import info.mukel.telegrambot4s.models.{ChatId, User}

trait Admins extends TelegramBot with Commands {
  // Insert a list of user ID's to add users as admins
  val allowed = List.empty[UserID]

  def isAllowed(user: User): Boolean = allowed.contains(UserID(user.id))

  def notifyAdmins(message: String): Unit = {
    for (admin <- allowed) {
      request(SendMessage(ChatId(admin.id), message, parseMode = Some(ParseMode.HTML)))
    }
  }
}
