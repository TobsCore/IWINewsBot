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
                       detailURLs: Set[String]) {

  override def toString: String = {
    var links = this.detailURLs
    var date = LocalDateTime.parseTimestamp(this.publicationDate)

    s"""<b>${this.title}</b>
       |
       |${this.description}
       |
       |Weitere Informationen unter:
       |${for (url <- links) {
         url
       }}
       |$date
     """.stripMargin
  }

}
