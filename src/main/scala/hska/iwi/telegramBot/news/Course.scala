package hska.iwi.telegramBot.news

/**
  * A course is represented as a Course class and not as a String in order to allow type checking.
  */
trait Course extends SubscribableMember {

  def getCourseByName(courseName: String): Option[Course] = {
    courseName.toUpperCase() match {
      case "MKIB" => Some(MKIB)
      case "INFB" => Some(INFB)
      case "INFM" => Some(INFM)
      case _      => None
    }
  }
}

object Course extends Course {}

object MKIB extends Course {
  override def toString: String = "MKIB"
}

object INFB extends Course {
  override def toString: String = "INFB"
}

object INFM extends Course {
  override def toString: String = "INFM"
}
