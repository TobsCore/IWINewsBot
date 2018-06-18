package hska.iwi.telegramBot.timetable

import hska.iwi.telegramBot.rooms.Room

case class BlockCourseSingleDate(comment: String,
                                 date: String,
                                 endTime: Int,
                                 id: Int,
                                 locations: Seq[Room],
                                 startTime: Int) {}
