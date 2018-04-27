package hska.iwi.telegramBot.timetable

case class TimetableEntry(courseOfStudies: String,
                          idSemester: Int,
                          moduleSpecialization: Int,
                          semester: Int,
                          semesterName: String,
                          timetables: Seq[SingleDayTimetable]) {}
