package hska.iwi.telegramBot.ChatBot.Routines

import com.rivescript.RiveScript
import com.rivescript.`macro`.Subroutine

class ExampleRoutine extends Subroutine {

  override def call(rs: RiveScript, args: Array[String]): String = {
    args.headOption match {
      case Some(param) => runWithParam(param, rs)
      case _           => "No parameter passed"
    }
  }

  private def runWithParam(param: String, rs: RiveScript): String = param match {
    case "id"    => s"Your ID is ${rs.currentUser()}"
    case "param" => "Parameter passed"
    case _       => "Parameter not recognized"
  }
}
