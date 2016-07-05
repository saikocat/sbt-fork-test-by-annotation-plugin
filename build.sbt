lazy val commonSettings = Seq(
  version in ThisBuild := "0.1.0",
  organization in ThisBuild := "com.saikocat"
)

lazy val root = (project in file(".")).
  settings(commonSettings).
  settings(
    sbtPlugin := true,
    name := "sbt-fork-test-by-annotation-plugin",
    description := "A sbt plugin to provide curried function to filter test marked with specific annotations and fork them",
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    scalacOptions := Seq("-deprecation", "-unchecked", "-encoding", "utf8"),
    publishMavenStyle := false,
    bintrayPackageLabels in bintray := Seq("fork", "annotation", "test"),
    bintrayRepository in bintray := "sbt-plugins",
    bintrayOrganization in bintray := None
  )
