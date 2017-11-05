package hska.iwi.telegramBot.news

import info.mukel.telegrambot4s.Implicits._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object EntryFormatter {
    def format(entry: Entry): String = {
      var formattedEntry: String = ""
      val formattedTitle = s"${entry.title.bold} \n \n"
      val formattedSubTitle = s"${entry.summary.replaceAll(": " + entry.content, "").bold} \n"
      val formattedSummary = s"${entry.content} \n \n"
      val formattedAuthorName = s"${entry.author.name.italic}\t (${entry.author.email}) \n"
      val formattedDate = new DateTime(entry.updated).toString(DateTimeFormat.forPattern("dd.MM.yyyy, HH:mm 'Uhr'"))

      formattedEntry = formattedEntry + formattedTitle + formattedSubTitle + formattedSummary + formattedAuthorName + formattedDate
      formattedEntry
    }
}
