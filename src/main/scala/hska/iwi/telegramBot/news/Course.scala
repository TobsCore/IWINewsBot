package hska.iwi.telegramBot.news

object Course extends Enumeration {
  type Course = Value
  val MKIB, INFB, INFM = Value

  def withNameOpt(s: String): Option[Course] = values.find(_.toString == s)

}
