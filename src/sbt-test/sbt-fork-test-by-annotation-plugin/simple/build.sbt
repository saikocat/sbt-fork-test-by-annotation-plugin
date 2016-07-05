import com.saikocat.sbtplugin.ForkTestByAnnotationPlugin._

lazy val check = TaskKey[Unit]("check", "Check all files were created and remove them.")

lazy val testsAnnotatedWithRequiresSpark: TaskKey[Seq[String]] =
  taskKey[Seq[String]]("Returns list of FQCNs of tests annotated with RequiresSpark")
testsAnnotatedWithRequiresSpark := {
  val analysis = (compile in Test).value
  def filterDefinition(annotationFullName: String): Boolean = {
    annotationFullName == "tags.RequiresSpark"
  }
  def isAnnotatedWithRequiresSpark(definition: xsbti.api.Definition): Boolean = {
    isAnnotatedWith(definition)(filterDefinition)
  }
  sourcesAnnotatedWith(analysis)(isAnnotatedWithRequiresSpark)
}


lazy val groupPrefix = "spark_"

lazy val root = (project in file(".")).
  settings(
    name := "foo",
    version := "0.1.0",
    scalaVersion := "2.10.6",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "2.2.6" % "test"
    ),
    testGrouping in Test <<= (definedTests in Test, testsAnnotatedWithRequiresSpark)
      .map { (testDefs, testsToFork) =>
        groupTestsToInProcessAndForkedJvm(
          testDefs, testsToFork,
          new sbt.ForkOptions(runJVMOptions = Seq(s"-Dgroup.prefix=$groupPrefix")))
      },
    check := {
      val groupSize = 3
      val groups = 1
      val files =
        for(i <- 0 until groups; j <- 1 to groupSize) yield
          file(groupPrefix + j)
      val (exist, absent) = files.partition(_.exists)
      exist.foreach(_.delete())
      if(absent.nonEmpty)
        sys.error("Files were not created:\n\t" + absent.mkString("\n\t"))
    }
  )

