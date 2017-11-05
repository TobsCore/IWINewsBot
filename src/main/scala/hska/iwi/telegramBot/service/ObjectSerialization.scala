package hska.iwi.telegramBot.service

import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization

trait ObjectSerialization {
  // To correctly serialize case classes
  implicit val formats = Serialization.formats(NoTypeHints)
}
