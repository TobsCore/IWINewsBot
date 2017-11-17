package hska.iwi.telegramBot.service

import com.redis.RedisClient
import com.redis.serialization.Parse.Implicits._
import com.typesafe.scalalogging.Logger
import hska.iwi.telegramBot.news.Course.Course
import hska.iwi.telegramBot.news.Entry
import hska.iwi.telegramBot.service.Implicits._
import info.mukel.telegrambot4s.models.User
import org.json4s.jackson.Serialization.write

import scala.collection.mutable

class RedisInstance(val redis: RedisClient) extends DBConnection with ObjectSerialization {

  val logger = Logger(getClass)

  def addUser(userID: UserID): Boolean = {
    redis.sadd("users", userID.id).getOrElse(0l).toInt == 1
  }

  def removeUser(userID: UserID): Boolean = {
    redis.srem("users", userID.id).getOrElse(0l).toInt == 1
  }

  def setUserData(userID: UserID, user: User): Boolean = {
    redis.set(s"user:${userID.id}", write(user))
  }

  def getUserData(userID: UserID): Option[User] = {
    redis.get[User](s"user:${userID.id}")
  }

  def removeUserData(userID: UserID): Boolean = {
    redis.del(s"user:${userID.id}").getOrElse(0l).toInt == 1
  }

  def setUserConfig(userID: UserID, userConfig: Map[Course, Boolean]): Boolean = {
    redis.hmset(s"config:${userID.id}", userConfig)
  }

  def setUserConfig(userID: UserID, course: Course, courseSetting: Boolean): Boolean = {
    redis.hset(s"config:${userID.id}", course, courseSetting)
  }

  def removeUserConfig(userID: UserID): Boolean = {
    redis.del(s"config:${userID.id}").getOrElse(0l).toInt == 1
  }

  def getConfigFor(userID: UserID): Option[Map[Course, Boolean]] = {
    redis.hgetall1[Course, Boolean](s"config:${userID.id}")
  }

  def getAllUserIDs: Option[Set[UserID]] = {
    val userList: Set[UserID] = redis.smembers[UserID]("users").getOrElse(Set()).flatten
    if (userList.isEmpty) {
      None
    } else {
      Some(userList)
    }
  }

  def userConfig(): Map[UserID, Option[Set[Course]]] = {
    val userIDs = getAllUserIDs.getOrElse(Set())
    val userSet = userIDs
      .map(
        userID =>
          userID -> getConfigFor(userID)
            .getOrElse(Map())
            .filter((p: (Course, Boolean)) => p._2)
            .keys
            .toSet[Course])
      .toMap[UserID, Set[Course]]

    userSet.mapValues(courses =>
      if (courses.nonEmpty) {
        Some(courses)
      } else {
        None
    })
  }

  def getConfigForUsers: Map[Course, Set[UserID]] =
    userConfig().par
      .filter((p: (UserID, Option[Set[Course]])) => p._2.isDefined)
      .flatMap((p: (UserID, Option[Set[Course]])) => p._2.get.toList.map(f => f -> p._1))
      .seq
      .groupBy(_._1)
      .par
      .map((p: (Course, Iterable[(Course, UserID)])) => (p._1, p._2.map(_._2).toSet))
      .seq

  def addNewsEntries(course: Course, newsEntrySet: Set[Entry]): Option[Set[Entry]] = {
    val resultSet: mutable.Set[Entry] = mutable.Set[Entry]()

    newsEntrySet.foreach(entry => {
      if (redis.sadd(s"news:$course", entry.id).getOrElse(0l).toInt == 1) {
        resultSet += entry
      }
    })
    if (resultSet.isEmpty) {
      None
    } else {
      Some(resultSet.toSet)
    }
  }
}

object RedisInstance {

  def default: RedisInstance =
    new RedisInstance(new RedisClient(Configuration.redisHost, Configuration.redisPort))
}
