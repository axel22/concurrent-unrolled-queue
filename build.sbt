name := "Concurrent unrolled queue"

version := "0.1"

mainClass in (Test, test) := Some("test.ConcurrentAddRemoveTest")

scalaVersion := "2.9.2"

scalacOptions ++= Seq("-deprecation", "-optimise")

