class ChatBot extends RiveScript(Config.utf8()) {
  val logger = Logger(getClass)

  private val folder = "chatbot"
  private val mainStream = getClass.getClassLoader.getResourceAsStream(s"$folder/main.rive")
  private val funnyAnswersStream = getClass.getClassLoader.getResourceAsStream(s"$folder/fun.rive")
  private val synonymsStream = getClass.getClassLoader.getResourceAsStream(s"$folder/synonyms.rive")
  private val arraysStream = getClass.getClassLoader.getResourceAsStream(s"$folder/arrays.rive")
  stream(Source.fromInputStream(mainStream).getLines.toArray)
  stream(Source.fromInputStream(funnyAnswersStream).getLines.toArray)
  stream(Source.fromInputStream(synonymsStream).getLines.toArray)
  stream(Source.fromInputStream(arraysStream).getLines.toArray)
  sortReplies()

  // Add Subroutines here
  setSubroutine("no-comprendo", new Routines.NoComprendoRoutine)
  setSubroutine("example", new Routines.ExampleRoutine)
  setSubroutine("timetable", new Routines.TimetableRoutine)
  setSubroutine("mensa", new Routines.MensaRoutine)
  setSubroutine("profs", new Routines.ProfsRoutine)
}