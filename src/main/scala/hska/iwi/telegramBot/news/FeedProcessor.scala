package hska.iwi.telegramBot.news

import hska.iwi.telegramBot.news.Course.Course

class FeedProcessor(feedReader: Map[Course, FeedReader]) {

  /**
    * Takes the feed that were passed in as constructor arguments and checks each feed for its
    * contents. The whole feed is placed in the map as a value, where the key is the feed
    * identifier. An identifier might be "INFB".
    *
    * @return A list, where the course is mapped to a set of entries. This set doesn't have to
    *         exist and can therefore be @code{None}, in a case where there is problem receiving
    *         the feeds. Otherwise the set will be empty.
    */
  def receiveNewEntries(): Map[Course, Option[Set[Entry]]] = {
    // TODO: Implement method
    Map(Course.MKIB -> None)
  }
}
