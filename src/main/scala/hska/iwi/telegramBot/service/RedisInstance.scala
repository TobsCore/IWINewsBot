package hska.iwi.telegramBot.service

import com.redis.RedisClient
import com.typesafe.scalalogging.Logger
import hska.iwi.telegramBot.news.Course
import hska.iwi.telegramBot.news.Course.Course
import info.mukel.telegrambot4s.models.User
import org.json4s.jackson.Serialization.write

object RedisInstance extends ObjectSerialization {
  val logger = Logger(getClass)
  val redis = new RedisClient(Configuration.redisHost, Configuration.redisPort)

  /**
    * Adds a user ID to the list of users.
    *
    * @param userID The user ID, which should be added to the list.
    * @return Returns `true` if the user has been added to the list. This means it has not been a
    *         user since then. Returns `false` if the user is already in the database.
    */
  def addUser(userID: UserID): Boolean = {
    redis.sadd("users", userID.id).getOrElse(0l).toInt == 1
  }

  /**
    * Removes a user ID from the list of users.
    *
    * @param userID The user ID, which should be removed from the list.
    * @return Returns `true` if the user has been removed from the list. Returns `false` if the
    *         user is not in the list of users.
    */
  def removeUser(userID: UserID): Boolean = {
    redis.srem("users", userID.id).getOrElse(0l).toInt == 1
  }

  /**
    * Sets the user data for the user ID.
    *
    * @param userID The user ID. This is used to locate the user.
    * @param user   The user object. This object is then serialized to JSON and stored in the
    *               database.
    * @return Returns `true` if the user has been set correctly (this includes updating). Returns
    *         `false` if an error occurred.
    */
  def setUserData(userID: UserID, user: User): Boolean = {
    redis.set(s"user:${userID.id}", write(user))
  }

  /**
    * Removes the user data for the given user ID.
    *
    * @param userID The user ID, for which the user data should be removed from the database.
    * @return Returns `true` if the user data has been removed from the list. Returns `false` if the
    *         user data has not been deleted (because it doesn't exist).
    */
  def removeUserData(userID: UserID): Boolean = {
    redis.del(s"user:${userID.id}").getOrElse(0l).toInt == 1
  }

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
  def setUserConfig(userID: UserID, userConfig: Map[Course, Boolean]): Boolean = {
    redis.hmset(s"config:${userID.id}", userConfig)
  }

  /**
    * Removes the user configuration from the database
    *
    * @param userID The user ID is used to identify the config and remove it accordingly.
    * @return Returns `true` if the config has been removed from the list. Returns `false` if the
    *         config has not been deleted (because it doesn't exist).
    */
  def removeUserConfig(userID: UserID): Boolean = {
    redis.del(s"config:${userID.id}").getOrElse(0l).toInt == 1
  }

  /**
    * Receives the subscription configuration for a given user ID.
    *
    * @param userID The user ID for which the subscription should be returned.
    * @return A Map, where each course is mapped to a boolean value. If no configuration can be
    *         found, `None` is returned.
    */
  def userConfig(userID: UserID): Option[Map[Course, Boolean]] = {
    val redisResult: Option[Map[String, String]] =
      redis.hgetall1[String, String](s"config:${userID.id}")

    redisResult match {
      case None => None
      case Some(map) =>
        try {
          Some(map.map { case (key, value) => Course.withNameOpt(key).get -> value.toBoolean })
        } catch {
          case e: Exception =>
            logger.error("Error converting config data to object")
            logger.debug(e.getMessage)
            None
        }
    }
  }

  /**
    * Returns a list of subscriptions for each user ID. These data are received from the database.
    * If a user has no subscription, the set
    * will be empty.
    *
    * @return A set of subscriptions for each user. If no subscription configuration can be
    *         received, `None` is returned.
    */
  def userConfig(): Map[UserID, Option[Set[Course]]] = {
    //TODO: Implement Method
    Map()
  }
}
