package hska.iwi.telegramBot

// Is used to write syntax such as '10 seconds' in akka calls. Otherwise warnings would be thrown during compilation.
import akka.actor._
import hska.iwi.telegramBot.commands.{AboSettings, About, Admin, Subscription}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}

import scala.io.Source
import scala.language.postfixOps

class IWINewsBot()
    extends TelegramBot
    with Polling
    with Commands
    with Callbacks
    with Subscription
    with Admin
    with AboSettings
    with About {

  // Put the token in file 'bot.token' in the root directly of this project. This will prevent the token from leaking
  lazy val token: String = scala.util.Properties
    .envOrNone("BOT_TOKEN")
    .getOrElse(Source.fromFile("bot.token").getLines().mkString)

  // Start the background feed reader.
  BackgroundFeedSync(token).start()
}

object IWINewsBot extends App {
  val bot = new IWINewsBot()
  bot.run()
}
