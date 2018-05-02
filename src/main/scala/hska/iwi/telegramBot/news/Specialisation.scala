package hska.iwi.telegramBot.news

trait Specialisation {

  def getSpecialisationByName(specialisationName: String): Option[Specialisation] =
    specialisationName.toLowerCase() match {
      case "softwareengineering" | "software engineering" | "software-engineering" | "swe" =>
        Some(SoftwareEngineering)
      case "interactive systems" | "interaktive systeme" | "interactive-systems" |
          "interaktive-systeme" =>
        Some(InteractiveSystems)
      case _ => None
    }
}

object Specialisation extends Specialisation {}

object SoftwareEngineering extends Specialisation {
  override def toString: String = "Software Engineering"
}

object InteractiveSystems extends Specialisation {
  override def toString: String = "Interaktive Systeme"
}
