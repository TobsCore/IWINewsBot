package hska.iwi.telegramBot.news

import scala.collection.mutable

class FeedProcessor(feedReader: FeedReader) {

  /**
    * Takes the feed that were passed in as constructor arguments and checks the feed for its
    * contents. The
    *
    * @return A list, where the course is mapped to a set of entries. This set doesn't have to
    *         exist and can therefore be `None`, in a case where there is problem receiving
    *         the feeds. Otherwise the set will be empty.
    */
  def receiveEntries(): Map[Course, Set[Entry]] = {
    val entryList: Set[Entry] = feedReader.receiveEntryList().getOrElse(Set())
    val mutableResultMap: mutable.Map[Course, Set[Entry]] = mutable.Map.empty[Course, Set[Entry]]

    for (entry <- entryList) {
      val entryCourses = entry.courseOfStudies.map(Course.getCourseByName(_).get)
      for (course <- entryCourses) {
        mutableResultMap.put(course, mutableResultMap.getOrElse(course, Set()) + entry)
      }
    }
    mutableResultMap.toMap
  }
}
