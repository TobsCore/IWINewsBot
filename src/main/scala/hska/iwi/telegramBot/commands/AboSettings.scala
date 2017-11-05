package hska.iwi.telegramBot.commands

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Extractors, TelegramBot}
import info.mukel.telegrambot4s.methods.EditMessageReplyMarkup
import info.mukel.telegrambot4s.models.{ChatId, InlineKeyboardButton, InlineKeyboardMarkup}

trait AboSettings extends Commands with Callbacks {
  _: TelegramBot =>

  var mkibAbo = false

  onCommand("/abo") { implicit msg =>
    {
      using(_.from) { user =>
        logger.info(s"User $user is changing settings.")
      }
      reply("Hier kannst Du festlegen, zu welchen Studiengängen Du Nachrichten erhalten möchtest.",
            replyMarkup = Some(markupCounter(0)))
    }
  }

  def markupCounter(n: Int): InlineKeyboardMarkup = {
    val btnText = if (mkibAbo) "MKIB [Ja]" else "MKIB [Nein]"
    logger.info(btnText)
    InlineKeyboardMarkup.singleButton(InlineKeyboardButton.callbackData(btnText, tag(n.toString)))
  }

  val mkibSelectionTAG = "MKIBSELECTION"

  private def tag = prefixTag(mkibSelectionTAG) _

  onCallbackWithTag(mkibSelectionTAG) { implicit cbq =>
    mkibAbo = !mkibAbo

    val message = if (mkibAbo) {
      "MKIB Nachrichten sind abonniert"
    } else {
      "MKIB Nachrichten sind abbestellt"
    }
    // Notification only shown to the user who pressed the button.
    ackCallback(Some(message))
    // Or just ackCallback()

    for {
      data <- cbq.data
      Extractors.Int(n) = data
      msg <- cbq.message
    } /* do */ {
      request(
        EditMessageReplyMarkup(chatId = Some(ChatId(msg.source)),
                               messageId = Some(msg.messageId),
                               replyMarkup = Some(markupCounter(n + 1))))
    }
  }
}
