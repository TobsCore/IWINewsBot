package hska.iwi.telegramBot.timetable

case class BlockCourseEntry(courseOfStudies: Seq[String],
                            id: Int,
                            lectureName: String,
                            lecturerIds: Seq[Int],
                            lecturerNames: Seq[String],
                            singleDates: Seq[BlockCourseSingleDate]) {}