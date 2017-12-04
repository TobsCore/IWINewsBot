package hska.iwi.telegramBot.rooms

case class FreeRoomEntry(day: Int, endTime: Int, locations: Set[Room], startTime: Int) {}
