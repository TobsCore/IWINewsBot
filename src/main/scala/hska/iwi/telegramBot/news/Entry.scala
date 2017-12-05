package hska.iwi.telegramBot.news

import hska.iwi.telegramBot.service.LocalDateTime

case class Entry(title: String,
                 subTitle: String,
                 courseOfStudies: Set[String],
                 publicationDate: String,
                 expirationDate: String,
                 content: String,
                 links: String,
                 newsType: String,
                 publicationTimestamp: String,
                 id: Int,
                 idOwner: Int,
                 nameOwner: String,
                 emailOwner: String) {

  /**
    * Formats a given entry as a Telegram Message with Markdown format.
    *
    * @return A formatted representation of the Entry as a String. This String can be used
    *         without any further modification to send it to recipients.
    */
  override def toString: String = {
    val date = LocalDateTime.parseTimestamp(this.publicationTimestamp)

    s"""<b>${this.title}</b>
       |
       |<b>${this.subTitle}</b>
       |${this.content}
       |
       |<i>${this.nameOwner}</i> (${this.emailOwner})
       |${date}""".stripMargin
  }
}
