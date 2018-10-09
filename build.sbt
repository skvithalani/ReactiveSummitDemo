name := "ReactiveSummitDemo"

version := "0.1"

scalaVersion := "2.12.6"

val akkaVersion = "2.5.15"

libraryDependencies += "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
libraryDependencies += "org.tmt" %% "csw-framework" % "0.5.0"