package hska.iwi.telegramBot.service

import org.json4s.{DateFormat, Formats, NoTypeHints, TypeHints}
import org.json4s.jackson.Serialization

trait ObjectSerialization {
  // To correctly serialize case classes
  implicit val formats: AnyRef with Formats {
    val typeHints: TypeHints

    val dateFormat: DateFormat
  } = Serialization.formats(NoTypeHints)
}
