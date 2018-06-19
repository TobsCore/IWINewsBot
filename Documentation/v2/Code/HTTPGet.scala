object HTTPGet {

  val logger = Logger(getClass)

  private val cache: Cache[String, Option[String]] = Scaffeine()
    .recordStats()
    .expireAfterWrite(1 day)
    .maximumSize(500)
    .build[String, Option[String]]()

  def getNow(address: String,
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
      val content = Source.fromInputStream(inputStream)(decoder).mkString
      if (inputStream != null) inputStream.close()
      Some(content)
    } catch {
      case ste: java.net.SocketTimeoutException =>
        logger.warn(s"Cannot connect to $address")
        logger.debug(ste.getMessage)
        None
      case e: java.io.IOException =>
        logger.warn(s"Cannot parse json correctly.")
        logger.debug(e.getMessage)
        None
    }
  }

  def cacheGet(address: String,
               connectTimeout: Int = 5000,
               readTimeout: Int = 5000,
               requestMethod: String = "GET"): Option[String] =
    cache.getIfPresent(address) match {
      case Some(e) =>
        logger.debug(s"Getting data out of cache, URL: $address")
        e
      case None =>
        logger.debug(s"Getting data from remote, URL: $address")
        val remoteContent = getNow(address)
        cache.put(address, remoteContent)
        remoteContent
  }
}
