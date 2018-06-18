package hska.iwi.telegramBot.service

object FeedURL {
  val bulletinBoard = "https://www.iwi.hs-karlsruhe.de/Intranetaccess/REST/newsbulletinboard"
  val mensa = "https://www.iwi.hs-karlsruhe.de/Intranetaccess/REST/canteen/v2/2/"
  val lecturer = "https://www.iwi.hs-karlsruhe.de/Intranetaccess/REST/lecturersconsultationhours"
  val facultyNews = "https://www.iwi.hs-karlsruhe.de/Intranetaccess/REST/faculty/news/texts/IWI"
  val timetable = "http://www.iwi.hs-karlsruhe.de/Intranetaccess/REST/timetable/"
  val alltimetables = "http://www.iwi.hs-karlsruhe.de/Intranetaccess/REST/timetable/all"
  val blockCourses = "http://www.iwi.hs-karlsruhe.de/Intranetaccess/REST/blockcourses/v2"

  val profslecturesurl =
    "http://www.iwi.hs-karlsruhe.de/Intranetaccess/REST/lecturersconsultationhours?images=false&lectures=true"

  val freeRooms =
    "https://www.iwi.hs-karlsruhe.de/Intranetaccess/REST/unoccupiedrooms/lecturehalls/now?suppress_error=true"
}
