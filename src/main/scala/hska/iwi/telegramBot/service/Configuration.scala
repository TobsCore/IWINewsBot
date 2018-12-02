package hska.iwi.telegramBot.service
import java.io.File

import scopt.OptionParser

/**
  * This is used to store common configuration elements.
  */
object Configuration {
  val redisHost = "localhost"
  val redisPort = 6379
  val redisTestPort = 6380
  val tokenFileName = "bot.token"
  var params: Option[Params] = None

  val parser: OptionParser[Params] = new scopt.OptionParser[Params]("IWINewsBot") {
    opt[File]('t', "token")
      .required()
      .valueName("<tokenfile>")
      .action((x, c) => c.copy(token = x))
      .text("token is a required file property")
  }

  def paramsAreValid(): Boolean = {
    if (params.isEmpty) {
      return false
    }
    if (!params.get.token.exists()) {
      println(s"File ${params.get.token.getAbsolutePath} doesn't exist.")
      return false
    }
    true
  }

  def tokenFile(): String = {
    params.get.token.getAbsolutePath
  }
}
