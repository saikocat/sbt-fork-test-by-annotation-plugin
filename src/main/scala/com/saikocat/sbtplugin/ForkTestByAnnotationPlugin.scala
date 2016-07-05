package com.saikocat.sbtplugin

import sbt.inc.Analysis
import sbt.{AutoPlugin, ForkOptions, TestDefinition}
import sbt.Tests.{Group, InProcess, SubProcess}
import xsbti.api.{Annotation, Definition, Id, Projection, Singleton, SimpleType}

object ForkTestByAnnotationPlugin extends AutoPlugin {

  def isAnnotatedWith(definition: Definition)
                     (annotationFilterFn: String => Boolean): Boolean = {
    definition.annotations().exists { annotation: Annotation =>
      annotation.base match {
        case projection: Projection => {
          matchPrefix(projection.prefix) { singleton =>
            val pathComponentsById = showPathComponentsById(singleton)
            annotationFilterFn(s"${pathComponentsById}.${projection.id()}")
          }
        }
        case _ => false
      }
    }
  }

  def matchPrefix(prefix: SimpleType)(matchedFn: Singleton => Boolean ): Boolean
      = prefix match {
    case singleton: Singleton => matchedFn(singleton)
    case _ => false
  }

  def showPathComponentsById(singleton: Singleton): String = {
    singleton.path.components
      .collect { case idType: Id => idType.id }
      .mkString(".")
  }

  def sourcesAnnotatedWith(analysis: Analysis)
                          (annotatedWithFn: Definition => Boolean): Seq[String] = {
    analysis.apis.internal.values.flatMap({ source =>
      source.api().definitions().filter(annotatedWithFn).map(_.name())
    }).toSeq
  }

  def groupTestsToInProcessAndForkedJvm(testDefs: Seq[TestDefinition],
                                        testsToFork: Seq[String],
                                        forkOptions: ForkOptions = ForkOptions()): Seq[Group] = {
    val (forkedTests, otherTests) = testDefs.partition {
      testDef => testsToFork.contains(testDef.name)
    }

    val inProcessTestsGroup =
      new Group(
        name = "Single JVM tests",
        tests = otherTests,
        runPolicy = InProcess)

    val forkedTestGroups = forkedTests map { test =>
      new Group(
        name = test.name,
        tests = Seq(test),
        runPolicy = SubProcess(forkOptions))
    }

    Seq(inProcessTestsGroup) ++ forkedTestGroups
  }

}
