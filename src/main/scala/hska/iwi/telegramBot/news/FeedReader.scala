package hska.iwi.telegramBot.news

import com.typesafe.scalalogging.Logger

import scalaj.http.{Http, HttpResponse}
import scala.xml.XML

class FeedReader(address: String) {
  val logger = Logger(getClass)

  def getEntries(): Option[List[Entry]] = {
    logger.debug(s"Connecting to $address")
    // get the xml content using scalaj-http
    val response: HttpResponse[String] = Http(address)
      .timeout(connTimeoutMs = 2000, readTimeoutMs = 20000)
      .asString
    val xmlString = response.body

    logger.debug(s"Request to $address returned HTTP Code ${response.code}")

    try {
      logger.trace(s"Retrieved the following: $xmlString")
      // convert the `String` to a `scala.xml.Elem`
      val xml = XML.loadString(xmlString)
      logger.debug("XML successfully parsed")

      // handle the xml as desired ...
      val entryNodes = xml \\ "entry"
      var entries = new scala.collection.mutable.ListBuffer[Entry]()
      for(elem <- entryNodes) {
        val title = (elem \\ "title").text
        val authorName = (elem \\ "author" \\ "name").text
        val authorEmail = (elem \\ "author" \\ "email").text
        val id = (elem \\ "id").text
        val updated = (elem \\ "updated").text
        val content = (elem \\ "content").text
        val summary = (elem \\ "summary").text
        val newEntry = Entry(title, Author(authorName, authorEmail), id, updated, content, summary)
        entries += newEntry
      }
      val entriesList = entries.toList
      Some(entriesList)
    } catch {
      case _: Exception =>
        logger.warn(s"Cannot parse XML")
        None
    }
  }
}