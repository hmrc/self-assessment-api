import play.sbt.routes.RoutesKeys._
import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object MicroServiceBuild extends Build with MicroService {

  val appName = "self-assessment-api"

  override lazy val plugins: Seq[Plugins] = Seq(
    SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin
  )

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
  override lazy val playSettings : Seq[Setting[_]] = Seq(
    routesImport += "uk.gov.hmrc.selfassessmentapi.resources.Binders._"
  )
}

private object AppDependencies {

  import play.sbt.PlayImport._
  import play.core.PlayVersion

  val compile = Seq(
    "uk.gov.hmrc" %% "play-reactivemongo" % "5.2.0",
    ws exclude("org.apache.httpcomponents", "httpclient") exclude("org.apache.httpcomponents", "httpcore"),
    "uk.gov.hmrc" %% "play-graphite" % "3.2.0",
    "uk.gov.hmrc" %% "microservice-bootstrap" % "5.12.0",
    "uk.gov.hmrc" %% "play-auth" % "1.0.0",
    "uk.gov.hmrc" %% "play-health" % "2.1.0",
    "uk.gov.hmrc" %% "play-url-binders" % "2.1.0",
    "uk.gov.hmrc" %% "play-config" % "4.2.0",
    "uk.gov.hmrc" %% "logback-json-logger" % "3.1.0",
    "uk.gov.hmrc" %% "domain" % "4.1.0",
    "uk.gov.hmrc" %% "play-hmrc-api" % "1.4.0",
    "uk.gov.hmrc" %% "play-hal" % "1.2.0",
    "uk.gov.hmrc" %% "play-scheduling" % "4.0.0",
    "ai.x" %% "play-json-extensions" % "0.8.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % "2.3.0" % scope,
        "org.scalatest" %% "scalatest" % "2.2.6" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % scope,
        "com.github.tomakehurst" % "wiremock" % "2.2.2" % scope,
        "uk.gov.hmrc" %% "reactivemongo-test"   % "2.0.0" % scope,
        "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "1.50.5" % scope,
        "org.mongodb" %% "casbah" % "3.1.1" % scope,
        "org.scalacheck" %% "scalacheck" % "1.12.6" % scope,
        "org.skyscreamer" % "jsonassert" % "1.4.0" % scope,
        "com.jayway.restassured" % "rest-assured" % "2.6.0" % scope,
        "org.mockito" % "mockito-core" % "1.9.5" % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "func"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % "2.0.0" % scope,
        "org.scalatest" %% "scalatest" % "2.2.6" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % scope,
        "com.github.tomakehurst" % "wiremock" % "2.2.2" % scope,
        "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "1.50.5" % scope,
        "org.mongodb" %% "casbah" % "3.1.1" % scope,
        // this line is only needed for coverage
        "org.scoverage" %% "scalac-scoverage-runtime" % "1.2.0" % scope,
        "org.mockito" % "mockito-core" % "1.9.5" % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}
