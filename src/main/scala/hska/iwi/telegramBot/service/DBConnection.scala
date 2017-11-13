package hska.iwi.telegramBot.service

import hska.iwi.telegramBot.news.Course.Course
import info.mukel.telegrambot4s.models.User

trait DBConnection {

  /**
    * Adds a user ID to the list of users.
    *
    * @param userID The user ID, which should be added to the list.
    * @return Returns `true` if the user has been added to the list. This means it has not been a
    *         user since then. Returns `false` if the user is already in the database.
    */
  def addUser(userID: UserID): Boolean

  /**
    * Removes a user ID from the list of users.
    *
    * @param userID The user ID, which should be removed from the list.
    * @return Returns `true` if the user has been removed from the list. Returns `false` if the
    *         user is not in the list of users.
    */
  def removeUser(userID: UserID): Boolean

  /**
    * Sets the user data for the user ID.
    *
    * @param userID The user ID. This is used to locate the user.
    * @param user   The user object. This object is then serialized to JSON and stored in the
    *               database.
    * @return Returns `true` if the user has been set correctly (this includes updating). Returns
    *         `false` if an error occurred.
    */
  def setUserData(userID: UserID, user: User): Boolean

  /**
    * Receives the user data from the database, if there are any saved.
    *
    * @param userID The user id, for which the userdata should be received.
    * @return The user object, that is saved for the given id. If no user can be returned,
    *         {{{None}}} will be returned.
    */
  def getUserData(userID: UserID): Option[User]

  /**
    * Removes the user data for the given user ID.
    *
    * @param userID The user ID, for which the user data should be removed from the database.
    * @return Returns `true` if the user data has been removed from the list. Returns `false` if the
    *         user data has not been deleted (because it doesn't exist).
    */
  def removeUserData(userID: UserID): Boolean

  /**
    * Stores the user configuration in the database. Configuration is the subscription settings.
    *
    * @param userID     The user ID is used to identify the configuration and map it to a user
    *                   (and vice versa).
    * @param userConfig The user configuration is a Key Value Map, which will be stored in the
    *                   database. The key is a study course (like INFB) and the value is a
    *                   boolean value, which indicates, whether the feed for this course should
    *                   be subscribed to.
    */
  def setUserConfig(userID: UserID, userConfig: Map[Course, Boolean]): Boolean

  /**
    * Sets the user configuration for one particular course. This setting can be either
    * {{{true}}} or {{{false}}}. If you want to set multiple courses at once, check out the other
    * method, which takes a map.
    *
    * @param userID        The user ID, for which the setting should be changed.
    * @param course        The course for which the setting will be changed.
    * @param courseSetting The setting, whether, the user should receive notifications for the
    *                      course. {{{true}}} means, that the user will receive notifications for
    *                      the given course, {{{false}}} unsubscribes the user for the given
    *                      course means, that the user will receive notifications for the given
    *                      course, {{{false}}} unsubscribes the user for the given course @return
    */
  def setUserConfig(userID: UserID, course: Course, courseSetting: Boolean): Boolean

  /**
    * Removes the user data for the given user ID.
    *
    * @param userID The user ID, for which the user data should be removed from the database.
    * @return Returns `true` if the user data has been removed from the list. Returns `false` if the
    *         user data has not been deleted (because it doesn't exist).
    */
  def removeUserConfig(userID: UserID): Boolean

  /**
    * Removes the user data for the given user ID.
    *
    * @param userID The user ID, for which the user data should be removed from the database.
    * @return Returns `true` if the user data has been removed from the list. Returns `false` if the
    *         user data has not been deleted (because it doesn't exist).
    */
  def getConfigFor(userID: UserID): Option[Map[Course, Boolean]]

  /**
    * Returns the list of all users that are stored in the database.
    *
    * @return Returns a set of all existing user IDs. If no ID can be found, {{{None}}} will be
    *         returned.
    */
  def getAllUserIDs: Option[Set[UserID]]

  /**
    * Returns a list of subscriptions for each user ID. These data are received from the database.
    * If a user has no subscription, the set
    * will be empty.
    *
    * @return A set of subscriptions for each user. If no subscription configuration has been
    *         stored in the database, `None` is returned.
    */
  def userConfig(): Map[UserID, Option[Set[Course]]]

  /**
    * Returns a map, where each course is mapped to a set of users, that are subscribed to the
    * course. If there are no users subscribed to the course, an empty set will be returned.
    * Different courses can point to the same user, since one user can be subscribed to multiple
    * courses.
    *
    * @return A set, where each course is mapped to the users that are subscribed to the course.
    */
  def getConfigForUsers: Map[Course, Set[UserID]]

}
