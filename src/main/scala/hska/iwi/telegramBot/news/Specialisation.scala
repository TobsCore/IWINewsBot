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

  def getShortCutByName(specialisation: Option[Specialisation]): Int = {
    if (specialisation.isDefined) {
      specialisation.get match {
        case SoftwareEngineering =>
          1
        case InteractiveSystems =>
          2
        case _ => 0
      }
    } else { 0 }
  }
}

object Specialisation extends Specialisation {}

object SoftwareEngineering extends Specialisation {
  override def toString: String = "Software-Engineering"
}

object InteractiveSystems extends Specialisation {
  override def toString: String = "Interaktive Systeme"
}
