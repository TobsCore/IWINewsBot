package hska.iwi.telegramBot.service

import com.redis.serialization.Parse
import hska.iwi.telegramBot.news.Course
import hska.iwi.telegramBot.news.Course.Course

object Implicits {
  implicit val userIDParser: Parse[UserID] = Parse[UserID](e => {
    val userID = new String(e, "UTF-8").toInt
    UserID(userID)
  })
  implicit val booleanParser: Parse[Boolean] = Parse[Boolean](new String(_, "UTF-8").toBoolean)
  implicit val courseParser: Parse[Course] = Parse[Course](e => {
    val courseAsString = new String(e, "utf-8")
    Course.withNameOpt(courseAsString).get
  })
}
