package hska.iwi.telegramBot.service

import com.redis.RedisClient

trait Instances {
  val redis: RedisClient = RedisInstance.redis

}
