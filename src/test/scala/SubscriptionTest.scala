import com.redis.RedisClient
import hska.iwi.telegramBot.news.{INFB, INFM, MKIB}
import hska.iwi.telegramBot.service.{Configuration, RedisInstance, UserID}
import info.mukel.telegrambot4s.models.User
import org.scalatest.{BeforeAndAfterAll, FunSuite}

class SubscriptionTest extends FunSuite with BeforeAndAfterAll {

  private val redisClient = new RedisClient(Configuration.redisHost, Configuration.redisTestPort)
  val redis = new RedisInstance(redisClient)

  val user1 = User(20, isBot = false, firstName = "SomeBot", lastName = Some("LastName"))
  val user2 = User(21, isBot = false, firstName = "OtherBot", None)
  val user1ID = UserID(user1.id)
  val user2ID = UserID(user2.id)

  val config = Map(INFB -> true, MKIB -> true, INFM -> true)

  override def beforeAll() {
    redis.addUser(user1ID)
    redis.addUser(user2ID)
  }

  test("Get user data") {
    val userID = user1ID

    redis.setUserData(user1ID, user1)
    redis.setUserData(user2ID, user2)
    val result = redis.getUserData(userID)
    assert(result.isDefined)
    assert(result.get.id == userID.id)
  }

  test("Userconfig for two users") {
    redis.setUserConfig(user1ID, config)
    redis.setUserConfig(user2ID, config)

    assertResult(
      Map(INFM -> Set(user1ID, user2ID),
          MKIB -> Set(user1ID, user2ID),
          INFB -> Set(user1ID, user2ID))) {
      redis.getConfigForUsers
    }
  }

  override def afterAll() {
    redis.removeUser(user1ID)
    redis.removeUserData(user1ID)
    redis.removeUserConfig(user1ID)
    redis.removeUser(user2ID)
    redis.removeUserData(user2ID)
    redis.removeUserConfig(user2ID)
  }

}
