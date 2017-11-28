package hska.iwi.telegramBot.news

import com.typesafe.scalalogging.Logger
import org.apache.commons.lang3.StringEscapeUtils
import org.json4s.{DefaultFormats, _}
import org.json4s.jackson.JsonMethods

case class FeedReader(address: String) {
  implicit val jsonDefaultFormats = DefaultFormats
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
    try {
      //response from bulletin board
      val content = get(address)

      //fixes encoding problem
      val jsonString = StringEscapeUtils.unescapeHtml4(content)

      //fixes problem which results from type being a scala keyword
      val updated = jsonString.replaceAll("type", "newsType")

      //parses the json entries and stores them in a set of entries
      val entries = JsonMethods.parse(updated).extract[Set[Entry]]
      logger.debug("JSON successfully parsed")

      Some(entries)
    } catch {
      case ste: java.net.SocketTimeoutException =>
        logger.info(s"Cannot connect to $address")
        logger.debug(ste.getMessage)
        None
      case e: java.io.IOException =>
        logger.warn(s"Cannot parse json correctly.")
        logger.debug(e.getMessage)
        None
    }
  }

  @throws(classOf[java.io.IOException])
  @throws(classOf[java.net.SocketTimeoutException])
  private def get(url: String,
                  connectTimeout: Int = 5000,
                  readTimeout: Int = 5000,
                  requestMethod: String = "GET") = {
    import java.net.{HttpURLConnection, URL}
    val connection = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
    connection.setConnectTimeout(connectTimeout)
    connection.setReadTimeout(readTimeout)
    connection.setRequestMethod(requestMethod)
    val inputStream = connection.getInputStream
    val content = io.Source.fromInputStream(inputStream).mkString
    if (inputStream != null) inputStream.close
    content
  }
}
