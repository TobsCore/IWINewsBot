package hska.iwi.telegramBot.service

import com.redis.RedisClient
import com.redis.serialization.Parse.Implicits._
import com.typesafe.scalalogging.Logger
import hska.iwi.telegramBot.news._
import hska.iwi.telegramBot.service.Implicits._
import hska.iwi.telegramBot.study.Study
import info.mukel.telegrambot4s.models.User
import org.json4s.jackson.Serialization.write

import scala.collection.mutable

class RedisInstance(val redis: RedisClient) extends DBConnection with ObjectSerialization {
  override def setDefaultUserConfig(user: UserID): Boolean = {
    setFacultyConfigForUser(user, configValue = true)
    setUserConfig(user, Map(INFB -> true, MKIB -> true, INFM -> true))
  }

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

  override def getFacultyConfig: Map[UserID, Option[Boolean]] = {
    val users = getAllUserIDs.getOrElse(Set())
    users.map(userID => (userID, getFacultyConfigForUser(userID))).toMap
  }

  override def removeFacultyConfigForUser(userID: UserID): Boolean = {
    redis.del(s"config:faculty:${userID.id}").getOrElse(0l).toInt == 1
  }

  override def addFacultyNews(newsEntries: List[FacultyNews]): List[FacultyNews] = {
    val newNews = mutable.Set.empty[FacultyNews]
    for (newsItem <- newsEntries) {
      val facultyNewsID = newsItem.hashCode4DB()
      if (redis.sadd("facultyNews", facultyNewsID).getOrElse(0l).toInt == 1) {
        redis.set(s"facultyNews:$facultyNewsID", newsItem.title)
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

  override def setPriceConfigForUser(priceConfig: PriceConfig, userID: UserID): Unit = {
    redis.set(s"config:price:${userID.id}", priceConfig.configValue)
  }

  override def getPriceConfigForUser(userID: UserID): PriceConfig = {
    PriceConfig(redis.get[String](s"config:price:${userID.id}").getOrElse("both"))
  }

  override def setStudySettingsForUser(user: UserID, study: Study): Boolean = {
    val settingsStore = Map("course" -> Some(study.course),
                            "specialisation" -> study.specialisation,
                            "semester" -> Some(study.semester)).collect {
      case (key, Some(value)) => key -> value
    }

    // Optionally reset specialisation, if it isn't set
    if (!settingsStore.contains("specialisation")) {
      redis.hdel(s"config:study:${user.id}", "specialisation")
    }

    redis.hmset(s"config:study:${user.id}", settingsStore)
  }

  override def getStudySettingsForUser(user: UserID): Option[Study] = {
    val course = redis.hget[Course](s"config:study:${user.id}", "course")
    val specialisation = redis.hget[Specialisation](s"config:study:${user.id}", "specialisation")
    val semester = redis.hget[Int](s"config:study:${user.id}", "semester")

    if (course.isDefined && semester.isDefined) {
      Some(Study(course.get, specialisation, semester.get))
    } else {
      None
    }
  }
}

object RedisInstance {
  val logger = Logger(getClass)

  def default: RedisInstance =
    try {
      new RedisInstance(new RedisClient(Configuration.redisHost, Configuration.redisPort))
    } catch {
      case _: Throwable =>
        logger.error("Couldn't find running Redis instance. Bot will exit now...")
        System.exit(1)
        null
    }

}
