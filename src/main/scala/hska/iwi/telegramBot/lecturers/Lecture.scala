package hska.iwi.telegramBot.lecturers

case class Lecture(comment: String,
                   contactHours: Int,
                   contents: String,
                   creditpoints: Int,
                   elective: Boolean,
                   english: Boolean,
                   exam: Option[Exam],
                   id: Int,
                   idLecture: Set[Integer],
                   internalName: String,
                   lectureType: String,
                   localizedLectureType: String,
                   longName: String,
                   material: String,
                   semester: Int,
                   weighting: Int,
                   workload: String) {}
