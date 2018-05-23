name := "IWINewsBot"
scalaVersion := "2.12.6"


// Scala Test
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

// Logback logging engine
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

// Telegram Bot API wrapper for scala
libraryDependencies += "info.mukel" %% "telegrambot4s" % "3.0.15"

// Redis as persistence
libraryDependencies += "net.debasishg" %% "redisclient" % "3.5"

// ScalaJ-HTTP for accessing remotes
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"

// Serialization and Deserialization
val json4sNative = "org.json4s" %% "json4s-native" % "3.6.0-M1"
val json4sJackson = "org.json4s" %% "json4s-jackson" % "3.6.0-M1"

// Date Formatter
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.18.0"

// For hashing
libraryDependencies ++= Seq("com.roundeights" %% "hasher" % "1.2.0")

// Rive Script (Chat Bots)
libraryDependencies += "com.rivescript" % "rivescript-core" % "0.10.0"

// Caching Library
libraryDependencies += "com.github.blemale" %% "scaffeine" % "2.5.0"

// Google Speech-to-Text
libraryDependencies += "com.google.cloud" % "google-cloud-speech" % "0.48.0-alpha"

mainClass in Compile := Some("hska.iwi.telegramBot.IWINewsBot")

// In order to run the application multiple times in sbt
run / fork := true
assembly / test := {}

// Stops the program from running in sbt (by calling ctrl+c) and doesn't stop sbt
Global / cancelable := true
