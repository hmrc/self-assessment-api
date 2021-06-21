/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package support

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.{ HeaderNames, MimeTypes, Status }
import play.api.inject.guice.{ GuiceApplicationBuilder, GuiceableModule }
import play.api.libs.ws.{ WSClient, WSRequest }
import play.api.test.{ DefaultAwaitTimeout, FutureAwaits }
import support.wiremock.{ WireMockConfig, WireMockSupport }

trait IntegrationBaseSpec
    extends AnyWordSpec
    with GuiceOneServerPerSuite
    with WireMockSupport
    with Matchers
    with Status
    with HeaderNames
    with MimeTypes
    with WireMockConfig
    with FutureAwaits
    with DefaultAwaitTimeout {

  private val mockHost: String = wiremockHost
  private val mockPort: String = wiremockPort.toString

  val releaseTwoEnabled: Boolean = false

  val fakeApplicationConfig: Map[String, Any] = Map(
    "auditing.enabled"                                 -> false,
    "bootstrap.http.headersAllowlist"                  -> Seq("Accept"),
    "internalServiceHostPatterns"                      -> Seq("localhost"),
    "appUrl"                                           -> "test-self-assessment-api-router",
    "microservice.services.service-locator.enabled"    -> false,
    "microservice.services.auth.host"                  -> mockHost,
    "microservice.services.auth.port"                  -> mockPort,
    "microservice.services.self-assessment-api.host"   -> mockHost,
    "microservice.services.self-assessment-api.port"   -> mockPort,
    "microservice.services.mtd-charitable-giving.host" -> mockHost,
    "microservice.services.mtd-charitable-giving.port" -> mockPort,
    "microservice.services.mtd-tax-calculation.host"   -> mockHost,
    "microservice.services.mtd-tax-calculation.port"   -> mockPort,
    "microservice.services.mtd-property-api.host"      -> mockHost,
    "microservice.services.mtd-property-api.port"      -> mockPort,
    "microservice.services.mtd-self-employment.host"   -> mockHost,
    "microservice.services.mtd-self-employment.port"   -> mockPort,
    "microservice.services.mtd-dividends-income.host"  -> mockHost,
    "microservice.services.mtd-dividends-income.port"  -> mockPort,
    "microservice.services.mtd-savings-accounts.host"  -> mockHost,
    "microservice.services.mtd-savings-accounts.port"  -> mockPort,
    "microservice.services.mtd-crystallisation.host"   -> mockHost,
    "microservice.services.mtd-crystallisation.port"   -> mockPort,
    "feature-switch.release-2.enabled"                 -> releaseTwoEnabled
  )

  // binding for ErrorHandlerISpec
  lazy val binding: Option[GuiceableModule] = None

  override implicit lazy val app: Application = binding match {
    case Some(value) =>
      GuiceApplicationBuilder()
        .bindings(value)
        .configure(fakeApplicationConfig + ("feature-switch.release-2.enabled" -> releaseTwoEnabled))
        .build()
    case None =>
      GuiceApplicationBuilder()
        .configure(fakeApplicationConfig + ("feature-switch.release-2.enabled" -> releaseTwoEnabled))
        .build()
  }

  lazy val client: WSClient = app.injector.instanceOf[WSClient]

  def buildRequest(path: String): WSRequest = client.url(s"http://localhost:$port$path").withFollowRedirects(false)
}
