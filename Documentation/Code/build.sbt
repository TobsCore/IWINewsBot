name := "IWINewsBot"
version := "0.4"
scalaVersion := "2.12.4"


// Scala Test
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"

// Logback logging engine
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

// Telegram Bot API wrapper for scala
libraryDependencies += "info.mukel" %% "telegrambot4s" % "3.0.14"

// Redis as persistence
libraryDependencies += "net.debasishg" %% "redisclient" % "3.4"

// ScalaJ-HTTP for accessing feed
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"

// Serialization and Deserialization
val json4sNative = "org.json4s" %% "json4s-native" % "3.6.0-M1"
val json4sJackson = "org.json4s" %% "json4s-jackson" % "3.6.0-M1"

// Removing encodings in rss feeds
libraryDependencies += "org.apache.commons" % "commons-text" % "1.1"

// Date Formatter
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.18.0"

// For hashing
libraryDependencies ++= Seq("com.roundeights" %% "hasher" % "1.2.0")

mainClass in Compile := Some("hska.iwi.telegramBot.IWINewsBot")

// In order to run the application multiple times in sbt
fork in run := true
test in assembly := {}

// Stops the program from running in sbt (by calling ctrl+c) and doesn't stop sbt
cancelable in Global := true
