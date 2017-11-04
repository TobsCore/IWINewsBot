package hska.iwi.telegramBot.news

import com.typesafe.scalalogging.Logger

import scalaj.http.{Http, HttpResponse}
import scala.xml.XML

class FeedReader(address: String) {
  val logger = Logger[FeedReader]

  def getEntries(): Option[List[Entry]] = {
    logger.debug(s"Connecting to $address")
    // get the xml content using scalaj-http
    val response: HttpResponse[String] = Http(address)
      .timeout(connTimeoutMs = 2000, readTimeoutMs = 20000)
      .asString
    val xmlString = response.body

    logger.trace(s"Request to $address returned HTTP Code ${response.code}")

    try {
      logger.trace(s"Retrieved the following: $xmlString")
      // convert the `String` to a `scala.xml.Elem`
      val xml = XML.loadString(xmlString)
      logger.debug("XML successfully parsed")

      // handle the xml as desired ...
      val titleNodes = (xml \\ "entry" \ "title")
      val headlines = for {
        t <- titleNodes
      } yield t.text
      Some(List())
    } catch {
      case e: Exception => {
        logger.warn(s"Cannot parse XML")
        None
      }
    }
  }
}