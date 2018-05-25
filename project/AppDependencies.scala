import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  private val hmrcTestVersion = "3.0.0"
  private val scalaTestVersion = "3.0.5"
  private val scalaTestPlusVersion = "2.0.1"
  private val pegdownVersion = "1.6.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-25" % "1.5.0",
    "uk.gov.hmrc" %% "auth-client" % "2.6.0",
    "uk.gov.hmrc" %% "play-hmrc-api" % "2.1.0"
  )

  def test(scope: String = "test") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
    "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
    "org.pegdown" % "pegdown" % pegdownVersion % scope,
    "org.mockito" % "mockito-core" % "2.18.3" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
  )

  def it(scope: String = "it") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
    "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
    "org.pegdown" % "pegdown" % pegdownVersion % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "com.github.tomakehurst" % "wiremock" % "2.17.0" % scope
  )

  def apply() = compile ++ test() ++ it()
}
