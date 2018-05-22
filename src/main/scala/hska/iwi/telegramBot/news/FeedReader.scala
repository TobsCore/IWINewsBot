package hska.iwi.telegramBot.news

import com.typesafe.scalalogging.Logger
import hska.iwi.telegramBot.service.HTTPGet
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, _}
import com.typesafe.scalalogging.Logger

case class FeedReader(address: String) {
  private implicit val jsonDefaultFormats: DefaultFormats.type = DefaultFormats
  val logger = Logger(getClass)

  /**
    * Connects to feed URL, which has been passed in the constructor and parses the received JSON
    * to a list of entry elements. If the returned json contains errors or couldn't been
    * transported correctly, `None` is returned.
    *
    * @return A list of entries of the current feed. `None`, if there was an error. The list may
    *         be empty.
    */
  def receiveEntryList(): Option[Set[Entry]] = {
    logger.debug(s"Connecting to $address")
    // Receive the feed from the web by http
    //response from bulletin board
    val content = HTTPGet.get(address)

    if (content.isDefined) {
      //fixes problem which results from type being a scala keyword
      val updated = content.get.replaceAll("type", "newsType")

      //parses the json entries and stores them in a set of entries
      val entries = JsonMethods.parse(updated).extract[Set[Entry]]
      logger.debug("JSON successfully parsed")

      Some(entries)
    } else {
      None
    }
  }

  def receiveFacultyNews(): Option[List[FacultyNews]] = {
    val content = HTTPGet.get(address)

    if (content.isDefined) {
      //parses the json entries and stores them in a set of entries
      val entries = JsonMethods.parse(content.get).extract[List[FacultyNews]]
      Some(entries)
    } else {
      None
    }
  }

}
