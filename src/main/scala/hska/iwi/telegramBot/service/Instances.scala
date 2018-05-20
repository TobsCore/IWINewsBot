package hska.iwi.telegramBot.service

trait Instances {

  val redis: RedisInstance = RedisInstance.default.getOrElse {
    println("Couldn't find running redis instance. Program will exit now...")
    System.exit(1)
    null
  }

}
