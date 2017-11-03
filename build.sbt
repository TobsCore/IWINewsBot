name := "IWINewsBot"
version := "0.1"
scalaVersion := "2.12.4"

// Logback logging engine
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

// In order to run the application multiple times in sbt
fork in run := true