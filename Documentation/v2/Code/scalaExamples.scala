trait Chat extends Commands {
 _: TelegramBot =>

 onMessage { implicit msg =>
   using(_.text) { input =>
     if (!input.startsWith("/")) {
       val user = msg.from.get
       // Is a chat message and not a command
       logger.trace(s"Received Chat Message: $input")
       // React to input accordingly
     }
   }
   }
}

