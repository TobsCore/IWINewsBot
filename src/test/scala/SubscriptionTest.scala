import com.redis.RedisClient
import hska.iwi.telegramBot.news.Course
import hska.iwi.telegramBot.service.{Configuration, RedisInstance, UserID}
import info.mukel.telegrambot4s.models.User
import ch.qos.logback.classic.{Level, Logger}
import org.slf4j.LoggerFactory
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite}

class SubscriptionTest extends FunSuite with BeforeAndAfterAll {

  val redis = new RedisInstance(
    new RedisClient(Configuration.redisHost, Configuration.redisTestPort))

  val logger: Unit = LoggerFactory
    .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    .asInstanceOf[Logger]
    .setLevel(Level.INFO)
  private val range: Range = 10000 until 500000

  /*override def beforeAll() {
    println("Setting up database")
    for (id <- range) {
      val userID = UserID(id)
      val user = User(id, isBot = false, "firstName", Some("lastname"), None, None)
      val config = Map(Course.INFB -> true, Course.MKIB -> true, Course.INFM -> true)

      redis.addUser(userID)
      redis.setUserData(userID, user)
      redis.setUserConfig(userID, config)
    }
  }*/

  test("Get user data") {
    val userID = UserID(13333)
    val result = redis.getUserData(userID)
    assert(result.get.id == 13333)
  }

  test("Get configuration") {
    println("Run speed test")
    time {
      val result = redis.getConfigForUsers
      assert(result.nonEmpty)
    }
  }

  /* override def afterAll() {
    println("Destructing database entries")
    for (id <- range) {
      val userID = UserID(id)
      redis.removeUser(userID)
      redis.removeUserData(userID)
      redis.removeUserConfig(userID)
    }
  }*/

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / 1000000 + "ms")
    result
  }
}
