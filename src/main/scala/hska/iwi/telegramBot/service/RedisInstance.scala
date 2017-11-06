package hska.iwi.telegramBot.service

import com.redis.RedisClient
import hska.iwi.telegramBot.news.Course.Course

object RedisInstance {
  val get = new RedisClient("localhost", 6379)

  /**
    * Returns a list of subscriptions for each user ID. These data are received from the database
    * . If a user has no subscription, the set
    * will be empty.
    *
    * @return A set of subscriptions for each user. If a user doesn't have a subscription, an
    *         empty set is returned.
    */
  def getUserConfig(): Map[UserID, Set[Course]] = {
    //TODO: Implement Method

    Map(UserID(1) -> Set())
  }
}
