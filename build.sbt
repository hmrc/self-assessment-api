import TestPhases.oneForkedJvmPerTest
import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "self-assessment-api-router"

lazy val scoverageSettings: Seq[Def.Setting[_]] = Seq(
  coverageExcludedPackages := "<empty>;.*(Reverse|BuildInfo|Routes).*",
  coverageMinimum := 80,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  coverageEnabled.in(ThisBuild, Test, test) := true
)

lazy val itTest = config("it").extend(Test)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .settings(
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(scoverageSettings: _*)
  .settings(publishingSettings: _*)
  .settings(
    Keys.fork in Test := true,
    javaOptions in Test += "-Dlogger.resource=logback-test.xml",
    scalaVersion := "2.11.11"
  )
  .configs(itTest)
  .settings(inConfig(itTest)(Defaults.testSettings))
  .settings(
    fork in itTest := false,
    unmanagedSourceDirectories in itTest := (baseDirectory in itTest) (base => Seq(base / "it")).value,
    testGrouping in itTest := oneForkedJvmPerTest((definedTests in itTest).value),
    parallelExecution in itTest := false,
    addTestReportOption(itTest, "int-test-reports")
  )
  .settings(
    resolvers += Resolver.jcenterRepo
  )
