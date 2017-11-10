package hska.iwi.telegramBot.news

/**
  * A course is represented as a Course class and not as a String in order to allow type checking.
  */
object Course extends Enumeration {
  type Course = Value
  val MKIB, INFB, INFM = Value

  /**
    * Checks, whether the given string is a course. If it is, it returns the course object,
    * otherwise {{{None}}} is returned. Note: This method will automatically uppercase the input
    * string, so "infB" will be passed as "INFB".
    *
    * @param s The string, that should be _casted_ to the Course object.
    * @return The corresponding Course object for the given input string. {{{None}}}, if no such
    *         entry is found.
    */
  def withNameOpt(s: String): Option[Course] = values.find(_.toString == s.toUpperCase)

}
