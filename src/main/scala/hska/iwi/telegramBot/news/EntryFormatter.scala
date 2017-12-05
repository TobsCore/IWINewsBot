package hska.iwi.telegramBot.news

import hska.iwi.telegramBot.service.LocalDateTime

object EntryFormatter {

  /**
    * Formats a given entry as a Telegram Message with Markdown format.
    *
    * @param entry The entry that should be formatted
    * @return A formatted representation of the Entry as a String. This String can be used
    *         without any further modification to send it to recipients.
    */
  def format(entry: Entry): String = {

    val date = LocalDateTime.parseTimestamp(entry.publicationTimestamp)

    s"""<b>${entry.title}</b>
       |
       |<b>${entry.subTitle}</b>
       |${entry.content}
       |
       |<i>${entry.nameOwner}</i> (${entry.emailOwner})
       |${date}""".stripMargin
  }
}
