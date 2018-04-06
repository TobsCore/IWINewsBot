package hska.iwi.telegramBot.ChatBot.Routines

import com.rivescript.RiveScript
import com.rivescript.`macro`.Subroutine

class ExampleRoutine extends Subroutine {
  override def call(rs: RiveScript, args: Array[String]): String = {
    s"Your telegram ID is ${rs.currentUser()}"
  }
}
