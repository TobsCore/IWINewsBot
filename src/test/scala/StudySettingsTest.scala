import com.redis.RedisClient
import hska.iwi.telegramBot.news._
import hska.iwi.telegramBot.service.{Configuration, RedisInstance, UserID}
import hska.iwi.telegramBot.study.Study
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.util.Try

class StudySettingsTest extends FunSuite with BeforeAndAfterAll {

  private val redisClient = new RedisClient(Configuration.redisHost, Configuration.redisTestPort)
  val redis = new RedisInstance(redisClient)

  val studySettingsINFBDefault = Study(INFB, None, 2)
  val studySettingsMKIBDefault = Study(MKIB, None, 5)
  val studySettingsINFMSWEDefault = Study(INFM, Some(SoftwareEngineering), 3)
  val studySettingsINFMInteractiveDefault = Study(INFM, Some(InteractiveSystems), 1)

  test("INFB") {
    val user = UserID(22)
    redis.setStudySettingsForUser(user, studySettingsINFBDefault)
    val receivedStudySettings = redis.getStudySettingsForUser(user)

    assert(receivedStudySettings.isDefined)
    assertResult(studySettingsINFBDefault.course) {
      receivedStudySettings.get.course
    }
    assertResult(studySettingsINFBDefault.semester) {
      receivedStudySettings.get.semester
    }
    assertResult(studySettingsINFBDefault.specialisation) {
      receivedStudySettings.get.specialisation
    }
  }

  test("MKIB") {
    val user = UserID(23)
    redis.setStudySettingsForUser(user, studySettingsMKIBDefault)
    val receivedStudySettings = redis.getStudySettingsForUser(user)

    assert(receivedStudySettings.isDefined)
    assertResult(studySettingsMKIBDefault.course) {
      receivedStudySettings.get.course
    }
    assertResult(studySettingsMKIBDefault.semester) {
      receivedStudySettings.get.semester
    }
    assertResult(studySettingsMKIBDefault.specialisation) {
      receivedStudySettings.get.specialisation
    }
  }

  test("INFM - Software-Engineering") {
    val user = UserID(24)
    redis.setStudySettingsForUser(user, studySettingsINFMSWEDefault)
    val receivedStudySettings = redis.getStudySettingsForUser(user)

    assert(receivedStudySettings.isDefined)
    assertResult(studySettingsINFMSWEDefault.course) {
      receivedStudySettings.get.course
    }
    assertResult(studySettingsINFMSWEDefault.semester) {
      receivedStudySettings.get.semester
    }
    assertResult(studySettingsINFMSWEDefault.specialisation) {
      receivedStudySettings.get.specialisation
    }
  }

  test("INFM - Interactive Systems") {
    val user = UserID(25)
    redis.setStudySettingsForUser(user, studySettingsINFMInteractiveDefault)
    val receivedStudySettings = redis.getStudySettingsForUser(user)

    assert(receivedStudySettings.isDefined)
    assertResult(studySettingsINFMInteractiveDefault.course) {
      receivedStudySettings.get.course
    }
    assertResult(studySettingsINFMInteractiveDefault.semester) {
      receivedStudySettings.get.semester
    }
    assertResult(studySettingsINFMInteractiveDefault.specialisation) {
      receivedStudySettings.get.specialisation
    }
  }

  test("Set MKIB after INFM") {
    val user = UserID(25)
    redis.setStudySettingsForUser(user, studySettingsINFMInteractiveDefault)
    redis.setStudySettingsForUser(user, studySettingsMKIBDefault)
    val receivedStudySettings = redis.getStudySettingsForUser(user)

    assert(receivedStudySettings.isDefined)
    assertResult(studySettingsMKIBDefault.course) {
      receivedStudySettings.get.course
    }
    assertResult(studySettingsMKIBDefault.semester) {
      receivedStudySettings.get.semester
    }
    assertResult(studySettingsMKIBDefault.specialisation) {
      receivedStudySettings.get.specialisation
    }
  }

  test("No settings set") {
    val user = UserID(99923)
    val receivedStudySettings = redis.getStudySettingsForUser(user)

    assert(receivedStudySettings.isEmpty)
  }

  test("Study ID for INFB") {
    val infbID = 0
    val receivedByObject: Try[Int] =
      Study.getID(studySettingsINFBDefault)
    assert(receivedByObject.isSuccess)
    assertResult(infbID)(receivedByObject.get)

    val receivedStudyInfo = Study.infoByID(receivedByObject.get)
    assert(receivedStudyInfo.isSuccess)
    assertResult((INFB, None))(receivedStudyInfo.get)
  }

  test("Study ID for MKIB") {
    val mkibID = 1
    val receivedByObject: Try[Int] =
      Study.getID(studySettingsMKIBDefault)
    assert(receivedByObject.isSuccess)
    assertResult(mkibID)(receivedByObject.get)

    val receivedStudyInfo = Study.infoByID(receivedByObject.get)
    assert(receivedStudyInfo.isSuccess)
    assertResult((MKIB, None))(receivedStudyInfo.get)
  }

  test("Study ID for INFM - Software Engineering") {
    val mkibID = 2
    val receivedByObject: Try[Int] =
      Study.getID(studySettingsINFMSWEDefault)
    assert(receivedByObject.isSuccess)
    assertResult(mkibID)(receivedByObject.get)

    val receivedStudyInfo = Study.infoByID(receivedByObject.get)
    assert(receivedStudyInfo.isSuccess)
    assertResult((INFM, Some(SoftwareEngineering)))(receivedStudyInfo.get)
  }

  test("Study ID for INFM - Interactive Systems") {
    val mkibID = 3
    val receivedByObject: Try[Int] =
      Study.getID(studySettingsINFMInteractiveDefault)
    assert(receivedByObject.isSuccess)
    assertResult(mkibID)(receivedByObject.get)

    val receivedStudyInfo = Study.infoByID(receivedByObject.get)
    assert(receivedStudyInfo.isSuccess)
    assertResult((INFM, Some(InteractiveSystems)))(receivedStudyInfo.get)
  }

  test("Study ID for INFB with set specialisation") {

    // This study is erroneous, since infb doesn't have a specialisation called Software Engineering
    val study = Study(INFB, Some(SoftwareEngineering), 3)
    val receivedByObject: Try[Int] = Study.getID(study)

    assert(receivedByObject.isFailure)
  }

  test("Illegal id for study") {
    val falseID = -1
    val received = Study.infoByID(falseID)

    assert(received.isFailure)
  }

}
