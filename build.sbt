name := "Concurrent unrolled queue"

version := "0.1"

mainClass in (Test, test) := Some("test.ConcurrentAddRemoveTest")

scalacOptions ++= Seq("-deprecation", "-optimise")

