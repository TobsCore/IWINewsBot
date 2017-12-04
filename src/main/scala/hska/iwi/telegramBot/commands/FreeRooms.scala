package hska.iwi.telegramBot.commands

import hska.iwi.telegramBot.news.FeedURL
import hska.iwi.telegramBot.rooms.{FreeRoomEntry, RoomFormatter}
import hska.iwi.telegramBot.service.{HTTPGet, ObjectSerialization}
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.methods.ParseMode
import org.json4s.jackson.JsonMethods

trait FreeRooms extends Commands {
  _: TelegramBot =>
  implicit val formats = org.json4s.DefaultFormats

  onCommand("/freieRaeume") { implicit msg =>
    logger.debug("received command 'freeRooms'")
    val content = HTTPGet.get(FeedURL.freeRooms)
    if (content.isDefined) {
      //parses the json entries and stores them in a MensaMoltke object
      val rooms = JsonMethods.parse(content.get).extract[FreeRoomEntry]
      reply(RoomFormatter.format(rooms), parseMode = Some(ParseMode.HTML))
    }
  }
}
