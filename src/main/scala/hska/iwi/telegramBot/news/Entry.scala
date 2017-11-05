package hska.iwi.telegramBot.news

case class Entry(title: String,
                 author: Author,
                 id: String,
                 updated: String,
                 content: String,
                 summary: String) {}
