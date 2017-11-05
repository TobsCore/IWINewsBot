package hska.iwi.telegramBot.service

import com.redis.RedisClient

object RedisInstance {
  val get = new RedisClient("localhost", 6379)
}
