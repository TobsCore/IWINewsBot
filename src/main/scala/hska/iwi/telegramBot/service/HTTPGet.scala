package hska.iwi.telegramBot.service

import java.net.{HttpURLConnection, URL}
import java.nio.charset.{Charset, CodingErrorAction}

import com.typesafe.scalalogging.Logger

object HTTPGet {

  val logger = Logger(getClass)

  def get(address: String,
          connectTimeout: Int = 5000,
          readTimeout: Int = 5000,
          requestMethod: String = "GET"): Option[String] = {
    try {
      val decoder = Charset.forName("UTF-8").newDecoder()
      decoder.onMalformedInput(CodingErrorAction.IGNORE)

      val connection = new URL(address).openConnection.asInstanceOf[HttpURLConnection]
      connection.setConnectTimeout(connectTimeout)
      connection.setReadTimeout(readTimeout)
      connection.setRequestMethod(requestMethod)
      val inputStream = connection.getInputStream
      val content = io.Source.fromInputStream(inputStream)(decoder).mkString
      if (inputStream != null) inputStream.close()
      Some(content)
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

}
