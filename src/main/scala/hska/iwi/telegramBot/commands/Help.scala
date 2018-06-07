package hska.iwi.telegramBot.commands

import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.methods.ParseMode

trait Help extends Commands {
  _: TelegramBot =>

  onCommand("/help") { implicit msg =>
    val aboutMessage =
      """Der IWINewsBot unterstützt folgende <b>Telegram-Befehle</b>:
        |/start - Starte den Erhalt der Abobenachrichtigungen des Bots.
        |/stop - Beende dein Nachrichten-Abo.
        |/mensa - Wähle für den nächsten 5 Tage das Mensaangebot aus.
        |/profs - Erhalte Informationen zu den Sprechzeiten und Kontaktdaten der Lehrenden.
        |/settings - Verwalte dein Nachrichten-Abo, deine Preispräferenz für die Mensa und deinen Studiengang für den Stundenplan.
        |/about - Erhalte Informationen über die Bot-Entwickler.
        |
        |Der Bot unterstützt <b>Chatbot-Funktionalitäten</b> im Bezug auf Mensa, Lehrende und Stundenplan. Probiere es doch einfach aus.
        |Mögliche Fragen könnten sein:
        |
        |<b>Mensa</b>
        |Was gibt es am Freitag in der Mensa?
        |Was gibt es morgen (Vegetarisches / Veganes / mit Schwein) zu essen?
        |Welche Gerichte enthalten (Fleisch / Fisch)?
        |Welche Gerichte sind (vegan / fleischlos)?
        |Welche Gerichte ohne Fleisch gibt es am Dienstag?
        |
        |<b>Lehrende</b>
        |Wie lautet die E-Mail von Professor Henning?
        |Wo finde ich Professor Henning?
        |Welche Vorlesungen hält Professor Henning?
        |
        |<b>Stundenplan</b>
        |Wie sieht mein Tag aus?
        |Welche Veranstaltungen habe ich am Montag?
        |In welchem Raum findet Computergrafik statt?
        |Wann findet Softwarearchitekuren statt?
      """.stripMargin
    reply(aboutMessage, parseMode = Some(ParseMode.HTML))
  }
}
