package hska.iwi.telegramBot.news

import info.mukel.telegrambot4s.Implicits._

object EntryFormatter {
    def format(entry: Entry): String = {
      var formattedEntry: String = ""
      val formattedTitle = s"${entry.title.bold} \n \n"
      val formattedSubTitle = s"${entry.summary.replaceAll(": " + entry.content, "").bold} \n"
      val formattedSummary = s"${entry.content} \n \n"
      val formattedAuthorName = s"${entry.author.name.italic}\t (${entry.author.email}) \n"
      //TODO parse the timestamp
      val formattedUpdated = entry.updated

      formattedEntry = formattedEntry + formattedTitle + formattedSubTitle + formattedSummary + formattedAuthorName + formattedUpdated
      formattedEntry
    }
}
