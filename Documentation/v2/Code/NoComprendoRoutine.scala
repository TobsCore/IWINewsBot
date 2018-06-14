class NoComprendoRoutine extends CustomSubroutine {

  val responses = List(
    "Das habe ich leider nicht verstanden.",
    "Ich weiß nicht, was du meinst. Eine Befehlsübersicht bekommst du über /help",
    "Da kann ich dir nicht helfen, sorry.",
  )

  override def call(rs: RiveScript, args: Array[String]): String = {
    logger.warn(ChatBotMarker(), s"No comprendo!")
    responses(scala.util.Random.nextInt(responses.size))
  }

}
