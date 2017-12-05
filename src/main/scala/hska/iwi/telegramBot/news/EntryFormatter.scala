package hska.iwi.telegramBot.news

import java.util.Locale

import hska.iwi.telegramBot.service.LocalDate
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

    val date = LocalDate.parseTimestamp(entry.publicationTimestamp)

    s"""<b>${entry.title}</b>
       |
       |<b>${entry.subTitle}</b>
       |${entry.content}
       |
       |<i>${entry.nameOwner}</i> (${entry.emailOwner})
       |${date}""".stripMargin
  }
}
