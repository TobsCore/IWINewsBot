package hska.iwi.telegramBot.service

case class PriceConfig(configValue: String) extends AnyVal {

  override def toString: String = {
    configValue match {
      case "student"  => "Studierende"
      case "employee" => "Mitarbeiter/innen"
      case "bot"      => "Studierende und Mitarbeiter/innen"
      case _          => "Studierende und Mitarbeiter/innen"
    }
  }
}
