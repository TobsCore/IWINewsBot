package hska.iwi.telegramBot.service

import info.mukel.telegrambot4s.models.User

trait Admins {
  val allowed = List(UserID(24154869), UserID(83972768))

  def isAllowed(user: User): Boolean = allowed.contains(UserID(user.id))
}
