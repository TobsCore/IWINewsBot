onMessage { implicit msg =>
  using(_.voice) { voice =>
    if (voice.duration > 7) {
      reply("Die Sprachnachricht darf nicht länger als 7 Sekunden sein.")
    } else if (redis.getQuotaForToday >= (1 hour)) { ~\label{line:quotaRedis}~
      logger.warn("Quota limit reached.")
      reply(
        "Es können keine weiteren Sprach-Nachrichten mehr empfangen werden, da die Quota an " +
        "Transkriptionen ausgeschöpft ist. Versuche es morgen wieder.")
    } else {
      ~\dots~  
    } 
~\dots~

request(GetFile(voice.fileId)).onComplete {
  case Success(file) =>
    file.filePath match {
      case Some(filePath) =>
        val url = s"https://api.telegram.org/file/bot$token/$filePath"
        ~\dots~      
    }
}
  
// Builds the sync recognize request
val config = RecognitionConfig
  .newBuilder()
  .setEncoding(AudioEncoding.OGG_OPUS)
  .setSampleRateHertz(opusFile.getInfo.getRate.toInt)
  .setLanguageCode("de-De")
  .build()
~\dots~
speechClient.get.recognize(config, audio).getResultsList.asScala.toList