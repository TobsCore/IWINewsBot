package hska.iwi.telegramBot.news

import hska.iwi.telegramBot.news.Course.Course

class FeedProcessor(feedReader: Map[Course, FeedReader]) {

  /**
    * Takes the feed that were passed in as constructor arguments and checks each feed for its
    * contents. The whole feed is placed in the map as a value, where the key is the feed
    * identifier. An identifier might be "INFB".
    *
    * @return
    */
  def receiveNewEntries(): Map[Course, Option[Set[Entry]]] = {
    // TODO: Implement method
    Map(Course.MKIB -> None)
  }
}
