name := "omdb-client"

version := "0.0.1"

scalaVersion := "2.11.8"

scalacOptions += "-feature"

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
  "org.scala-lang" % "scala-reflect" % "2.11.8",
  "io.spray" %% "spray-client" % "1.3.3",
  "io.spray" %% "spray-json" % "1.3.2",
  "com.typesafe.akka" %% "akka-actor" % "2.4.1",
  "com.typesafe.akka" %% "akka-actor" % "2.4.1",
  "org.scalatest" %% "scalatest" % "3.0.0-M15" % "test")
