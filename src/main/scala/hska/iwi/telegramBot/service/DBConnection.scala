package hska.iwi.telegramBot.service

import hska.iwi.telegramBot.news.Course.Course
import info.mukel.telegrambot4s.models.User

trait DBConnection {
  def addUser(userID: UserID): Boolean

  def removeUser(userID: UserID): Boolean

  def setUserData(userID: UserID, user: User): Boolean

  def removeUserData(userID: UserID): Boolean

  def setUserConfig(userID: UserID, userConfig: Map[Course, Boolean]): Boolean

  def setUserConfig(userID: UserID, course: Course, courseSetting: Boolean): Boolean

  def removeUserConfig(userID: UserID): Boolean

  def getConfigFor(userID: UserID): Option[Map[Course, Boolean]]

  def getAllUserIDs: Option[Set[UserID]]

  def userConfig(): Map[UserID, Option[Set[Course]]]

  def getConfigForUsers: Map[Course, Set[UserID]]

  def getUserData(userID: UserID): Option[User]
}
