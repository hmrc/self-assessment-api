import play.PlayImport.PlayKeys._
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
    routesImport += "uk.gov.hmrc.selfassessmentapi.controllers.Binders._"
  )
}

private object AppDependencies {

  import play.PlayImport._
  import play.core.PlayVersion

  private val microserviceBootstrapVersion = "4.2.1"
  private val playAuthVersion = "3.1.0"
  private val playHealthVersion = "1.1.0"
  private val playJsonLoggerVersion = "2.1.1"
  private val playUrlBindersVersion = "1.0.0"
  private val playConfigVersion = "2.0.1"
  private val domainVersion = "3.7.0"
  private val hmrcTestVersion = "1.6.0"
  private val playReactivemongoVersion = "4.4.0"
  private val playHmrcApiVersion = "0.4.0"
  private val playHalVersion = "0.3.0"

  val compile = Seq(
    "uk.gov.hmrc" %% "play-reactivemongo" % playReactivemongoVersion,
    ws exclude("org.apache.httpcomponents", "httpclient") exclude("org.apache.httpcomponents", "httpcore"),
    "uk.gov.hmrc" %% "microservice-bootstrap" % microserviceBootstrapVersion,
    "uk.gov.hmrc" %% "play-authorisation" % playAuthVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-url-binders" % playUrlBindersVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
    "uk.gov.hmrc" %% "play-json-logger" % playJsonLoggerVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "play-hmrc-api" % playHmrcApiVersion,
    "uk.gov.hmrc" %% "play-hal" % playHalVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % "2.2.6" % scope,
        "org.pegdown" % "pegdown" % "1.5.0" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus" %% "play" % "1.2.0" % scope,
        "com.github.tomakehurst" % "wiremock" % "1.54" % scope,
        "uk.gov.hmrc" %% "reactivemongo-test"   % "1.2.0" % scope,
        "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "1.50.3" % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "func"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % "2.2.6" % scope,
        "org.pegdown" % "pegdown" % "1.5.0" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus" %% "play" % "1.2.0" % scope,
        "com.github.tomakehurst" % "wiremock" % "1.54" % scope,
        "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "1.50.3" % scope,
        // this does not get automatically added by the plugin
        "org.scoverage" % "scalac-scoverage-runtime_2.11" % "1.1.1" % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}

