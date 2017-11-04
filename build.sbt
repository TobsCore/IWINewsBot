name := "IWINewsBot"
version := "0.1"
scalaVersion := "2.12.4"

// Logback logging engine
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

// Telegram Bot API wrapper for scala
libraryDependencies += "info.mukel" %% "telegrambot4s" % "3.0.14"

// Redis as persistence
libraryDependencies += "net.debasishg" %% "redisclient" % "3.4"

// XML
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.6"

// ScalaJ-HTTP for accessing feed
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"

// Serialization and Deserialization
val json4sNative = "org.json4s" %% "json4s-native" % "{latestVersion}"
val json4sJackson = "org.json4s" %% "json4s-jackson" % "{latestVersion}"


mainClass in Compile := Some("hska.iwi.telegramBot.IWINewsBot")

// In order to run the application multiple times in sbt
fork in run := true

// Stops the program from running in sbt (by calling ctrl+c) and doesn't stop sbt
cancelable in Global := true
