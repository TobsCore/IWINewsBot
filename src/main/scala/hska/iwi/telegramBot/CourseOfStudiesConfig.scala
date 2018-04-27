package hska.iwi.telegramBot

import hska.iwi.telegramBot.news.Course

case class CourseOfStudiesConfig(courseOfStudies: Course, specOrGroup: String, semester: Int) {

  trait specialisationCourse {

    def getShortCutByName(spec: String): Option[String] = {
      spec.toLowerCase() match {
        case "softwareengineering" => Some("1")
        case "interaktivesysteme"  => Some("2")
        case "all"                 => Some("0")
        case _                     => None
      }
    }
  }

}
