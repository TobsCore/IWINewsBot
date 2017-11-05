package hska.iwi.telegramBot.news

import com.typesafe.scalalogging.Logger
import org.apache.commons.lang3.StringEscapeUtils

import scalaj.http.{Http, HttpResponse}
import scala.xml.XML

class FeedReader(address: String) {
  val logger = Logger(getClass)

  /**
    * Connects to feed URL, which has been passed in the constructor and parses the received XML to a list of entry elements. If the returned xml contains errors or couldn't been transported correctly, @code{None} is returned.
    *
    * @return A list of entries of the current feed. @code{None}, if there was an error. The list may be empty.
    */
  def receiveEntryList(): Option[List[Entry]] = {
    logger.debug(s"Connecting to $address")
    // Receive the XML feed from the web by http
    val response: HttpResponse[String] = Http(address)
      .charset("windows-1252")
      .timeout(connTimeoutMs = 2000, readTimeoutMs = 20000)
      .asString

    // Get rid of html entities, like codes for Umlaute.
    val xmlString = StringEscapeUtils.unescapeHtml4(response.body)

    logger.debug(s"Request to $address returned HTTP Code ${response.code}")

    try {
      logger.trace(s"Retrieved the following: $xmlString")
      // convert the `String` to a `scala.xml.Elem`
      val xml = XML.loadString(xmlString)
      logger.debug("XML successfully parsed")

      // Parse the xml objects and put them in a list
      val entryNodes = xml \\ "entry"
      var entries = new scala.collection.mutable.ListBuffer[Entry]()
      for (elem <- entryNodes) {
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
      case e: Exception =>
        logger.warn(s"Cannot parse XML")
        logger.debug(e.getMessage)
        None
    }
  }
}
