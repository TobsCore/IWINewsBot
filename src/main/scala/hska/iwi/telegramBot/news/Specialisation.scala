package hska.iwi.telegramBot.news

trait Specialisation {

  def getSpecialisationByName(specialisationName: String): Option[Specialisation] =
    specialisationName.toLowerCase() match {
      case "softwareengineering" | "software engineering" | "software-engineering" | "swe" =>
        Some(SoftwareEngineering)
      case "medieninformatik"=>
        Some(Medieninformatik)
      case "machine learning" | "maschinelles lernen" | "maschinelles-lernen" |
          "machine-learning" =>
        Some(MachineLearning)
      case _ => None
    }

  def getShortCutByName(specialisation: Option[Specialisation]): Int = {
    if (specialisation.isDefined) {
      specialisation.get match {
        case SoftwareEngineering =>
          3
        case Medieninformatik =>
          4
        case MachineLearning =>
          5
        case _ => 0
      }
    } else { 0 }
  }
}

object Specialisation extends Specialisation {}

object SoftwareEngineering extends Specialisation {
  override def toString: String = "Software-Engineering"
}

object Medieninformatik extends Specialisation {
  override def toString: String = "Medieninformatik"
}

object MachineLearning extends Specialisation {
  override def toString: String = "Maschinelles Lernen"
}
