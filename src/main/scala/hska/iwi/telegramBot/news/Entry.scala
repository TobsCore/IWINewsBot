package hska.iwi.telegramBot.news

import hska.iwi.telegramBot.news.Course.Course

case class Entry(title: String,
                 subTitle: String,
                 courseOfStudies: Set[String],
                 publicationDate: String,
                 expirationDate: String,
                 content: String,
                 links: String,
                 newsType: String,
                 id: Int,
                 idOwner: Int,
                 nameOwner: String,
                 emailOwner: String) {}
