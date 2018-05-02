package hska.iwi.telegramBot.service

import com.redis.serialization.Parse
import hska.iwi.telegramBot.news.{Course, Specialisation}
import info.mukel.telegrambot4s.models.User
import org.json4s.jackson.Serialization.read

object Implicits extends ObjectSerialization {
  implicit val userIDParser: Parse[UserID] = Parse[UserID](e => {
    val userID = new String(e, "UTF-8").toInt
    UserID(userID)
  })
  implicit val booleanParser: Parse[Boolean] = Parse[Boolean](new String(_, "UTF-8").toBoolean)
  implicit val courseParser: Parse[Course] = Parse[Course](e => {
    val courseAsString = new String(e, "utf-8")
    Course.getCourseByName(courseAsString).get
  })
  implicit val specialisationParser: Parse[Specialisation] = Parse[Specialisation](e => {
    val specialisationAsString = new String(e, "utf-8")
    val result = Specialisation.getSpecialisationByName(specialisationAsString)
    if (result.isEmpty) {
      throw new IllegalArgumentException(s"$specialisationAsString is not valid")
    }
    result.get
  })
  implicit val userData: Parse[User] = Parse[User](e => {
    read[User](new String(e, "utf-8"))
  })
}
