package hska.iwi.telegramBot.ChatBot.Marker

import org.slf4j.{Marker, MarkerFactory}

/**
  * The marker is used in order to log statements that are important for the chatbot. This may
  * include logging chatbot-messages to a separate file.
  */
object ChatBotMarker {
  def apply(): Marker = MarkerFactory.getMarker("Chatbot")
}
