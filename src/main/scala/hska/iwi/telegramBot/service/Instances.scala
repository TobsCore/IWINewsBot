package hska.iwi.telegramBot.service

import com.redis.RedisClient

trait Instances {
  val redis: RedisInstance = RedisInstance.default

}
