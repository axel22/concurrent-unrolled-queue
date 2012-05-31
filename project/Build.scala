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
    val javacommand = "java -Xmx2048m -Xms2048m -XX:+UseCondCardMark -server -cp %s:%s:%s".format(
      dp.map(_.data).mkString(":"),
      jar,
      testjar
    )
    javacommand
  }

  val benchTask = InputKey[Unit](
    "bench",
    "Runs a specified benchmark."
  ) <<= inputTask {
    (argTask: TaskKey[Seq[String]]) =>
      (argTask, javaCommand) map {
        (args, jc) =>
          val javacommand = jc
          val comm = javacommand + " " + args.mkString(" ")
          println("Executing: " + comm)
          comm!
      }
  }



  lazy val root = Project(id = "concurrent-unrolled-queue",
                          base = file("."),
                          settings = Project.defaultSettings ++ Seq(javaCommandSetting, benchTask))
}
