package hska.iwi.telegramBot.news

case class Entry(title: String,
                 subTitle: String,
                 courseOfStudies: Set[String],
                 publicationDate: String,
                 expirationDate: String,
                 content: String,
                 links: String,
                 newsType: String,
                 publicationTimestamp: String,
                 id: Int,
                 idOwner: Int,
                 nameOwner: String,
                 emailOwner: String) {}
