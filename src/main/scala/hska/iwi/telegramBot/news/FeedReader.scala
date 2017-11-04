package hska.iwi.telegramBot.news

import scalaj.http.{Http, HttpResponse}
import scala.xml.XML

class FeedReader(address: String) {

  def getEntries(): List[Entry] = {
    // get the xml content using scalaj-http
    val response: HttpResponse[String] = Http(address)
      .timeout(connTimeoutMs = 2000, readTimeoutMs = 5000)
      .asString
    val xmlString = response.body

    // convert the `String` to a `scala.xml.Elem`
    val xml = XML.loadString(xmlString)

    // handle the xml as desired ...
    val titleNodes = (xml \\ "entry" \ "title")
    val headlines = for {
      t <- titleNodes
    } yield t.text
    List()
  }
}