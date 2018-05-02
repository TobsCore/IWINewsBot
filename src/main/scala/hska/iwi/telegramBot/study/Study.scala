package hska.iwi.telegramBot.study

import hska.iwi.telegramBot.news._

case class Study(course: Course, specialisation: Option[Specialisation] = None, semester: Int) {

  /**
    * For a given id returns the combination of course (i.e INFB) and specialisation (i.e.
    * SoftwareEngineering) if one exists. If there is no speicalisation (i.e. INFB doesn't
    * currently has a specialisation), None ist returned.
    *
    * @param id A valid id, must be => 0.
    * @return A tuple which constist of the course and the specialisation. The specialisation is
    *         wrappen in an Option for cases where no specialisation exists. The whole result is
    *         wrappen in an either, for when the given id is not valid (i.e. doesn't match an
    *         output).
    */
  def infoByID(id: Int): Either[IllegalArgumentException, (Course, Option[Specialisation])] =
    id match {
      case 0 => Right((INFB, None))
      case 1 => Right((MKIB, None))
      case 2 => Right((INFM, Some(SoftwareEngineering)))
      case 3 => Right((INFM, Some(InteractiveSystems)))
      case _ => Left(new IllegalArgumentException(s"$id is not a valid identifier."))
    }
}
