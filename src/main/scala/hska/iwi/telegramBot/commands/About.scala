package hska.iwi.telegramBot.commands

import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands

trait About extends Commands {
  _: TelegramBot =>

  onCommand("/about") { implicit msg =>
    val aboutMessage =
      """|Diese App wurde von Anna-Lena Schwarzkopf und Tobias Kerst im Rahmen ihrer Projektarbeit entwickelt.
         |Bei Fragen oder weiterem Feedback, bitten wir um eine E-Mail an scan1091@hs-karlsruhe.de oder keto1018@hs-karlsruhe.de""".stripMargin
    reply(aboutMessage)
  }
}
