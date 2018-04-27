package hska.iwi.telegramBot.timetable

import hska.iwi.telegramBot.rooms.Room

case class LectureEntry(cancellation: String,
                        comment: String,
                        contactHours: Int,
                        creditPoInts: Int,
                        day: Int,
                        duration: String,
                        endTime: Int,
                        examinationRegulationsNumber: Int,
                        firstDate: String,
                        group: String,
                        id: Int,
                        idCourseOfStudiesType: String,
                        idLecture: Int,
                        idLecturers: Seq[Int],
                        internalName: String,
                        interval: String,
                        lastChange: String,
                        lastDate: String,
                        lectureName: String,
                        lecturerNames: Seq[String],
                        locations: Seq[Room],
                        semester: Int,
                        startTime: Int,
                        title: String) {}
