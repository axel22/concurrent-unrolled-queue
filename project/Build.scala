import sbt._
import Keys._

object CUQBuild extends Build {
  val javaCommand = TaskKey[String](
    "java-command",
    "Creates a java vm command for launching a process."
  )

  val javaCommandSetting = javaCommand <<= (
    dependencyClasspath in Compile,
    artifactPath in (Compile, packageBin),
    artifactPath in (Test, packageBin),
    packageBin in Compile,
    packageBin in Test
  ) map {
    (dp, jar, testjar, pbc, pbt) => // -XX:+UseConcMarkSweepGC -XX:-DoEscapeAnalysis -XX:MaxTenuringThreshold=12 -verbose:gc -XX:+PrintGCDetails 
    val javacommand = "java -Xmx4096m -Xms4096m -XX:+UseCondCardMark -server -cp %s:%s:%s".format(
      dp.map(_.data).mkString(":"),
      jar,
      testjar
    )
    javacommand
  }

  val benchTask = InputKey[Unit](
    "bench",
    "Runs a specified benchmark. Usage: bench numberOfElements [numberOfThreads] microbench.<benchName> [numberOfRuns]"
  ) <<= inputTask {
    (argTask: TaskKey[Seq[String]]) =>
      (argTask, javaCommand) map {
        (args, jc) =>
          val nElements = "-Dbench.elements=" + args(0)
          val nThreads = if (args(1).charAt(0).isDigit) "-Dbench.threads=" + args(1) else ""
          val javacommand = jc + " " + nElements + " " + nThreads
          val program = if (nThreads == "") args.tail else args.tail.tail
          val comm = javacommand + " " + program.mkString(" ")
//          println(comm)
          comm!
      }
  }

  lazy val root = Project(id = "concurrent-unrolled-queue",
                          base = file("."),
                          settings = Project.defaultSettings ++ Seq(javaCommandSetting, benchTask))
}

