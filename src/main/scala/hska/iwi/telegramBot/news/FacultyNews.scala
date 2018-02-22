package hska.iwi.telegramBot.news

import com.roundeights.hasher.Hasher

import scala.language.postfixOps

case class FacultyNews(date: String,
                       title: String,
                       description: String,
                       image: String,
                       imageType: String,
                       imageURL: String,
                       caption: String,
                       publicationDate: String,
                       detailUrls: List[String]) {

  /**
    * Since FacultyNews items don't have a unique ID, which is needed to identify them, this hash
    * is used to identify news. Only the title and date are used to differentiate between them
    * and since SHA256 is a pretty good solution for such scenarios, this algorithm is used.
    * First, the date is appended to the title, then the hash is calculated, which is then
    * returned as the hexadecimal representation.
    *
    * @return A string representation of the object.
    */
  def hashCode4DB(): String = Hasher(title + date).sha256.hex

  override def toString: String = {
    val resultText = new StringBuilder(s"""<b>${this.title}</b>
         |${if (date == null) { "" } else { date + "\n" }}
         |${this.description}
         |""".stripMargin)

    if (detailUrls.nonEmpty) {
      resultText.append("\nWeitere Informationen unter:")
      for (url <- detailUrls) {
        resultText.append("\n" + url)
      }
    }
    resultText.toString
  }

}
