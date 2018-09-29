package hska.iwi.telegramBot.study

import hska.iwi.telegramBot.news._

import scala.util.{Failure, Try}

case class Study(course: Course, semester: Int) {
  override def toString: String = {
    s"$semester. Semester $course"
  }
}

object Study {

  /**
    * For a given study object, this will return the id. Semesters will be ignored, so only the
    * course and the specialisation are checked.
    *
    * @param study A valid study object.
    * @return The id for the given study. If no matching study can be found, {{{Failure}}} will
    *         be returned.
    */
  def getID(study: Study): Try[Int] = study match {
    case Study(INFB, _)                      => Try(0)
    case Study(MKIB, _)                      => Try(1)
    case Study(INFM, _) => Try(2)
    case _ =>
      Failure(
        new IllegalArgumentException(s"$study is not a valid study object. Cannot pattern match"))
  }

  /**
    * Just a wrapper method for the other getID method.
    *
    * @param course         The course of the study
    * @return The id for the given study. If no matching study can be found, {{{Failure}}} will
    *         be returned.
    */
  def getID(course: Course): Try[Int] =
    getID(Study(course, 0))

  /**
    * For a given id returns the combination of course (i.e INFB) and specialisation (i.e.
    * SoftwareEngineering) if one exists. If there is no speicalisation (i.e. INFB doesn't
    * currently has a specialisation), {{{None}}} ist returned.
    *
    * @param id A valid id, must be => 0.
    * @return The Course for the corresponding course-identifier. If the course-identifier cannot
    *         be mapped to a correct Course, a failure is returned.
    */
  def infoByID(id: Int): Try[Course] =
    id match {
      case 0 => Try(INFB)
      case 1 => Try(MKIB)
      case 2 => Try(INFM)
      case _ => Failure(new IllegalArgumentException(s"$id is not a valid identifier."))
    }
}
