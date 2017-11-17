package hska.iwi.telegramBot.news

import java.util.Locale

import info.mukel.telegrambot4s.Implicits._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object EntryFormatter {

  /**
    * Formats a given entry as a Telegram Message with Markdown format.
    *
    * @param entry The entry that should be formatted
    * @return A formatted representation of the Entry as a String. This String can be used
    *         without any further modification to send it to recipients.
    */
  def format(entry: Entry): String = {
    val title = entry.title
    val splitSummary = entry.summary.split(": ", 2)
    val subTitle = if (splitSummary.size >= 2) {
      "\n" + splitSummary(0)
    } else {
      ""
    }
    val summary = entry.content
    val authorName = entry.author.name
    val authorEmail = entry.author.email
    val date =
      new DateTime(entry.updated)
        .toString(DateTimeFormat.forPattern("d. MMM yyyy - HH:mm 'Uhr'").withLocale(Locale.GERMAN))

    s"""|<b>$title</b>
        |<b>$subTitle</b>
        |$summary
        |
        |<i>$authorName</i>  ($authorEmail)
        |$date""".stripMargin
  }
}
