package hska.iwi.telegramBot.news

import hska.iwi.telegramBot.service.LocalDateTime

case class FacultyNews(date: String,
                       title: String,
                       description: String,
                       image: String,
                       imageType: String,
                       imageURL: String,
                       caption: String,
                       publicationDate: String,
                       detailUrls: List[String]) {

  override def toString: String = {
    val date = LocalDateTime.parseTimestamp(this.publicationDate)
    val resultText = new StringBuilder(s"""<b>${this.title}</b>
         |
       |${this.description}
         |""".stripMargin)

    if (detailUrls.size >= 1) {
      resultText.append("\nWeitere Informationen unter:\n")
      for (url <- detailUrls) {
        resultText.append(url + "\n")
      }
    }
    resultText.append(s"\n$date")
    resultText.toString()
  }

}
