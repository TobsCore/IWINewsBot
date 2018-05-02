package hska.iwi.telegramBot.study

import hska.iwi.telegramBot.news._

import scala.util.{Failure, Try}

case class Study(course: Course, specialisation: Option[Specialisation] = None, semester: Int) {}

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
    case Study(INFB, None, _)                      => Try(0)
    case Study(MKIB, None, _)                      => Try(1)
    case Study(INFM, Some(SoftwareEngineering), _) => Try(2)
    case Study(INFM, Some(InteractiveSystems), _)  => Try(3)
    case _ =>
      Failure(
        new IllegalArgumentException(s"$study is not a valid study object. Cannot pattern match"))
  }

  /**
    * For a given id returns the combination of course (i.e INFB) and specialisation (i.e.
    * SoftwareEngineering) if one exists. If there is no speicalisation (i.e. INFB doesn't
    * currently has a specialisation), {{{None}}} ist returned.
    *
    * @param id A valid id, must be => 0.
    * @return A tuple which constist of the course and the specialisation. The specialisation is
    *         wrappen in an Option for cases where no specialisation exists. The whole result is
    *         wrappen in an either, for when the given id is not valid (i.e. doesn't match an
    *         output).
    */
  def infoByID(id: Int): Try[(Course, Option[Specialisation])] =
    id match {
      case 0 => Try((INFB, None))
      case 1 => Try((MKIB, None))
      case 2 => Try((INFM, Some(SoftwareEngineering)))
      case 3 => Try((INFM, Some(InteractiveSystems)))
      case _ => Failure(new IllegalArgumentException(s"$id is not a valid identifier."))
    }
}
