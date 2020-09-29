import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % "1.5.0",
    "uk.gov.hmrc" %% "play-hmrc-api"     % "4.1.0-play-26",
    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.6.0" cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % "1.6.0" % Provided cross CrossVersion.full
  )

  def test(scope: String = "test, it") = Seq(
    "org.scalatest"          %% "scalatest"             % "3.2.0"             % scope,
    "org.scalacheck"         %% "scalacheck"            % "1.14.2"            % scope,
    "com.vladsch.flexmark"   % "flexmark-all"           % "0.35.10"           % scope,
    "org.scalamock"          %% "scalamock"             % "5.0.0"             % scope,
    "org.scalatestplus"      %% "scalatestplus-mockito" % "1.0.0-M2"          % scope,
    "com.typesafe.play"      %% "play-test"             % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"    % "3.1.2"             % scope,
    "org.mockito"            %  "mockito-core"          % "3.2.4"             % scope,
    "com.github.tomakehurst" % "wiremock"               % "2.27.2"            % scope
  )

  // Fixes a transitive dependency clash between wiremock and scalatestplus-play
  val overrides: Seq[ModuleID] = {
    val jettyFromWiremockVersion = "9.2.24.v20180105"
    Seq(
      "org.eclipse.jetty"           % "jetty-client"       % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-continuation" % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-http"         % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-io"           % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-security"     % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-server"       % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-servlet"      % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-servlets"     % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-util"         % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-webapp"       % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-xml"          % jettyFromWiremockVersion,
      "org.eclipse.jetty.websocket" % "websocket-api"      % jettyFromWiremockVersion,
      "org.eclipse.jetty.websocket" % "websocket-client"   % jettyFromWiremockVersion,
      "org.eclipse.jetty.websocket" % "websocket-common"   % jettyFromWiremockVersion
    )
  }
}
