import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._


trait MicroService {

  import uk.gov.hmrc._
  import DefaultBuildSettings._
  import TestPhases._

  val appName: String

  lazy val appDependencies : Seq[ModuleID] = ???
  lazy val plugins : Seq[Plugins] = Seq(play.PlayScala)
  lazy val playSettings : Seq[Setting[_]] = Seq.empty

  lazy val FuncTest = config("func") extend Test
  
  lazy val microservice = Project(appName, file("."))
    .enablePlugins(Seq(play.PlayScala) ++ plugins : _*)
    .settings(playSettings : _*)
    .settings(scalaSettings: _*)
    .settings(publishingSettings: _*)
    .settings(defaultSettings(): _*)
    .settings(
      targetJvm := "jvm-1.8",
      scalaVersion := "2.11.8",
      // use lint option to avoid missing string interpolation warnings https://github.com/playframework/playframework/issues/5134
      // remove when we upgrade to play 2.5.x https://github.com/playframework/playframework/pull/5135
      scalacOptions ++= Seq("-Xlint:-missing-interpolator",
                            "-Xfatal-warnings",
                            "-deprecation",
                            "-encoding", "UTF-8",
                            "-unchecked",
                            "-language:postfixOps",
                            "-language:implicitConversions",
                            "-Ywarn-numeric-widen",
                            "-Yno-adapted-args"),
      libraryDependencies ++= appDependencies,
      parallelExecution in Test := false,
      fork in Test := false,
      retrieveManaged := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
    )
    .configs(FuncTest)
    .settings(inConfig(FuncTest)(Defaults.testSettings): _*)
    .settings(
      Keys.fork in FuncTest := false,
      unmanagedSourceDirectories in FuncTest <<= (baseDirectory in FuncTest)(base => Seq(base / "func")),
      unmanagedClasspath in FuncTest += baseDirectory.value / "resources",
      unmanagedClasspath in Runtime += baseDirectory.value / "resources",
      unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
      addTestReportOption(FuncTest, "int-test-reports"),
      testGrouping in FuncTest := oneForkedJvmPerTest((definedTests in FuncTest).value),
      parallelExecution in FuncTest := false)
    .settings(resolvers += Resolver.bintrayRepo("hmrc", "releases"))
}

private object TestPhases {

  def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
    tests map {
      test => new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }
}
