package hska.iwi.telegramBot

// Is used to write syntax such as '10 seconds' in akka calls. Otherwise warnings would be thrown
// during compilation.
import hska.iwi.telegramBot.commands._
import hska.iwi.telegramBot.service.Configuration
import hska.iwi.telegramBot.service.Params
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import org.json4s.DefaultFormats

import scala.io.Source
import scala.language.postfixOps

class IWINewsBot()
    extends TelegramBot
    with Polling
    with Commands
    with Callbacks

    // These are our commands
    with Subscription
    with Admin
    with Settings
    with About
    with Help
    with Mensa
    with Lecturers
    with Chat {

  override implicit val jsonDefaultFormats: DefaultFormats.type = org.json4s.DefaultFormats

  // Put the token in file 'bot.token' in the root directly of this project. This will prevent
  // the token from leaking
  lazy val token: String = Source.fromFile(Configuration.tokenFile()).getLines().mkString

  // Start the background feed reader.
  BackgroundFeedSync(token).start()
}

object IWINewsBot extends App {

  Configuration.params = Configuration.parser.parse(args, Params())
  if (!Configuration.paramsAreValid()) {
    System.exit(1)
  }

  val bot = new IWINewsBot()
  bot.run()
}
