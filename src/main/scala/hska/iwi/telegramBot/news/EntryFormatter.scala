package hska.iwi.telegramBot.news

import java.util.Locale

import info.mukel.telegrambot4s.Implicits._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object EntryFormatter {

  def format(entry: Entry): String = {
    val title = entry.title.bold
    val subTitle = entry.summary.replaceAll(": " + entry.content, "").bold
    val summary = entry.content
    val authorName = entry.author.name.italic
    val authorEmail = entry.author.email
    val date =
      new DateTime(entry.updated)
        .toString(DateTimeFormat.forPattern("d. MMM. yyyy - HH:mm 'Uhr'").withLocale(Locale.GERMAN))

    s"""|$title
        |
        |$subTitle
        |$summary
        |
        |$authorName ($authorEmail)
        |$date""".stripMargin
  }
}
