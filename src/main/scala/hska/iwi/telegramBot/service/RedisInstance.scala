package hska.iwi.telegramBot.service

import com.redis.RedisClient
import com.redis.serialization.Parse.Implicits._
import com.typesafe.scalalogging.Logger
import hska.iwi.telegramBot.news.{Course, Entry, FacultyNews}
import hska.iwi.telegramBot.service.Implicits._
import info.mukel.telegrambot4s.models.User
import org.json4s.jackson.Serialization.write

import scala.collection.mutable

class RedisInstance(val redis: RedisClient) extends DBConnection with ObjectSerialization {

  val logger = Logger(getClass)

  override def addUser(userID: UserID): Boolean = {
    redis.sadd("users", userID.id).getOrElse(0l).toInt == 1
  }

  override def removeUser(userID: UserID): Boolean = {
    redis.srem("users", userID.id).getOrElse(0l).toInt == 1
  }

  override def isMember(userID: UserID): Boolean = {
    redis.sismember("users", userID.id)
  }

  override def setUserData(userID: UserID, user: User): Boolean = {
    redis.set(s"user:${userID.id}", write(user))
  }

  override def getUserData(userID: UserID): Option[User] = {
    redis.get[User](s"user:${userID.id}")
  }

  override def removeUserData(userID: UserID): Boolean = {
    redis.del(s"user:${userID.id}").getOrElse(0l).toInt == 1
  }

  override def setUserConfig(userID: UserID, userConfig: Map[Course, Boolean]): Boolean = {
    redis.hmset(s"config:${userID.id}", userConfig)
  }

  override def setUserConfig(userID: UserID, course: Course, courseSetting: Boolean): Boolean = {
    redis.hset(s"config:${userID.id}", course, courseSetting)
  }

  override def removeUserConfig(userID: UserID): Boolean = {
    redis.del(s"config:${userID.id}").getOrElse(0l).toInt == 1
  }

  override def getConfigFor(userID: UserID): Option[Map[Course, Boolean]] = {
    redis.hgetall1[Course, Boolean](s"config:${userID.id}")
  }

  override def setFacultyConfigForUser(userID: UserID, configValue: Boolean): Unit = {
    redis.set(s"config:faculty:${userID.id}", configValue)
  }

  override def getFacultyConfigForUser(userID: UserID): Option[Boolean] = {
    redis.get[Boolean](s"config:faculty:${userID.id}")
  }

  override def getFacultyConfig(): Map[UserID, Option[Boolean]] = {
    val users = getAllUserIDs.getOrElse(Set())
    users.map(userID => (userID, getFacultyConfigForUser(userID))).toMap
  }

  override def removeFacultyConfigForUser(userID: UserID): Boolean = {
    redis.del(s"config:faculty:${userID.id}").getOrElse(0l).toInt == 1
  }

  override def addFacultyNews(newsEntries: List[FacultyNews]): List[FacultyNews] = {
    val newNews = mutable.Set.empty[FacultyNews]
    for (newsItem <- newsEntries) {
      if (redis.sadd("facultyNews", newsItem.hashCode4DB()).getOrElse(0l).toInt == 1) {
        newNews += newsItem
      }
    }

    newNews.toList
  }

  override def getAllUserIDs: Option[Set[UserID]] = {
    val userList: Set[UserID] = redis.smembers[UserID]("users").getOrElse(Set()).flatten
    if (userList.isEmpty) {
      None
    } else {
      Some(userList)
    }
  }

  override def userConfig(): Map[UserID, Option[Set[Course]]] = {
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

  override def getConfigForUsers: Map[Course, Set[UserID]] = {
    val userToCourse = userConfig()
      .filter((p: (UserID, Option[Set[Course]])) => p._2.isDefined)
      .map((p: (UserID, Option[Set[Course]])) => p._2.get.toList.map(f => f -> p._1))

    // In order to merge the Lists which have the same keys, which should map to a set of user
    // ids, a mutable map is used to add the elements to.
    val mutableResultMap: mutable.Map[Course, Set[UserID]] = mutable.Map.empty

    userToCourse.foreach(l =>
      l.foreach((e: (Course, UserID)) => {
        mutableResultMap.put(e._1, mutableResultMap.getOrElse(e._1, Set()) + e._2)
      }))

    mutableResultMap.toMap
  }

  override def addNewsEntries(course: Course, newsEntrySet: Set[Entry]): Set[Entry] = {
    val resultSet: mutable.Set[Entry] = mutable.Set[Entry]()

    newsEntrySet.foreach(entry => {
      if (redis.sadd(s"news:$course", entry.id).getOrElse(0l).toInt == 1) {
        resultSet += entry
      }
    })
    resultSet.toSet
  }

}

object RedisInstance {

  def default: RedisInstance =
    new RedisInstance(new RedisClient(Configuration.redisHost, Configuration.redisPort))
}
