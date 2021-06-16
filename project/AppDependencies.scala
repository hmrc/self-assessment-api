import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile = Seq(
    ws,
    "uk.gov.hmrc"   %% "bootstrap-backend-play-28" % "5.3.0"
  )

  def test(scope: String = "test, it") = Seq(
    "org.scalatest"          %% "scalatest"             % "3.2.9"             % scope,
    "org.scalacheck"         %% "scalacheck"            % "1.15.4"            % scope,
    "com.vladsch.flexmark"   % "flexmark-all"           % "0.36.8"           % scope,
    "org.scalamock"          %% "scalamock"             % "5.1.0"             % scope,
    "org.scalatestplus"      %% "scalatestplus-mockito" % "1.0.0-M2"          % scope,
    "com.typesafe.play"      %% "play-test"             % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"    % "5.1.0"             % scope,
    "org.mockito"            %  "mockito-core"          % "3.6.28"             % scope,
    "com.github.tomakehurst" % "wiremock-jre8"          % "2.27.2"            % scope
  )
}
