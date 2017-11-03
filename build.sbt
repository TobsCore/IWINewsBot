name := "IWINewsBot"
version := "0.1"
scalaVersion := "2.12.4"

// Logback logging engine
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

// Telegram Bot API wrapper for scala
libraryDependencies += "info.mukel" %% "telegrambot4s" % "3.0.14"

mainClass in Compile := Some("hska.iwi.telegramBot.IWINewsBot")

// In order to run the application multiple times in sbt
fork in run := true

// Stops the progamm from running in sbt (by calling ctrl+c) and doesn't stop sbt
cancelable in Global := true
