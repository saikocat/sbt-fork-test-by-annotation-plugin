# sbt-fork-test-by-annotation-plugin
A sbt plugin to provide curried function to filter test marked with specific annotations and fork them

# Installation
For sbt 0.13.6+ add sbt-assembly as a dependency in `project/assembly.sbt`:

```
addSbtPlugin("com.saikocat" % "sbt-fork-test-by-annotation-plugin" % "0.1.0")
```

# Usage
In `build.sbt` or `Build.scala`

```
import com.saikocat.sbtplugin.ForkTestByAnnotationPlugin._

// Define a task key to find tests to fork
lazy val testsAnnotatedWithRequiresSpark: TaskKey[Seq[String]] =
  taskKey[Seq[String]]("Returns list of FQCNs of tests annotated with RequiresSpark")

testsAnnotatedWithRequiresSpark := {
  val analysis = (compile in Test).value

  // Define a filter function that will match a predicate
  // Full qualified path is required
  // e.g: for a list of tags filtering
  // List("tags.RequiresSpark", "tags.RequiresHive").contains(annotationFullName)
  def filterDefinition(annotationFullName: String): Boolean = {
    annotationFullName == "tags.RequiresSpark"
  }

  // Filter the Tests Definition
  def isAnnotatedWithRequiresSpark(definition: xsbti.api.Definition): Boolean = {
    isAnnotatedWith(definition)(filterDefinition)
  }

  // Trigger the source filtering
  sourcesAnnotatedWith(analysis)(isAnnotatedWithRequiresSpark)
}

// Group the tests to non-forks and forks
// Here you can define the fork options as well
testGrouping in Test <<= (definedTests in Test, testsAnnotatedWithRequiresSpark)
  .map { (testDefs, testsToFork) =>
    groupTestsToInProcessAndForkedJvm(
      testDefs, testsToFork,
      new sbt.ForkOptions(runJVMOptions = Seq(s"-Dgroup.prefix=someProp)))
  }
```


# Reference
This project took a huge inspiration from this article
[SBT: Group annotated tests to run in forked JVMs](http://chariotsolutions.com/blog/post/sbt-group-annotated-tests-run-forked-jvms)

Sweetness that was absent in the article:
* Fully curried functions for any kind of source filtering
* Support fully qualified annotation class name to avoid class name conflict
* Configurable ForkOptions
* Tested (see `sbt-test` folder)
* Shareable with other projects (plugin itself)

# License
Published under The MIT License, see LICENSE
