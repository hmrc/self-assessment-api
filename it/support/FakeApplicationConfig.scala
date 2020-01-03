/*
 * Copyright 2020 HM Revenue & Customs
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

import support.wiremock.WireMockConfig

trait FakeApplicationConfig { _: WireMockConfig =>

  val fakeApplicationConfig: Map[String, Any] = Map(
    "auditing.enabled" -> false,
    "httpHeadersWhitelist" -> Seq("Accept"),
    "internalServiceHostPatterns" -> Seq("localhost"),
    "appUrl" -> "test-self-assessment-api-router",
    "microservice.services.service-locator.enabled" -> false,
    "microservice.services.auth.host" -> wiremockHost,
    "microservice.services.auth.port" -> wiremockPort,
    "microservice.services.self-assessment-api.host" -> wiremockHost,
    "microservice.services.self-assessment-api.port" -> wiremockPort,
    "microservice.services.mtd-charitable-giving.host" -> wiremockHost,
    "microservice.services.mtd-charitable-giving.port" -> wiremockPort,
    "microservice.services.mtd-tax-calculation.host" -> wiremockHost,
    "microservice.services.mtd-tax-calculation.port" -> wiremockPort,
    "microservice.services.mtd-property-api.host" -> wiremockHost,
    "microservice.services.mtd-property-api.port" -> wiremockPort,
    "microservice.services.mtd-self-employment.host" -> wiremockHost,
    "microservice.services.mtd-self-employment.port" -> wiremockPort,
    "microservice.services.mtd-dividends-income.host" -> wiremockHost,
    "microservice.services.mtd-dividends-income.port" -> wiremockPort,
    "microservice.services.mtd-savings-accounts.host" -> wiremockHost,
    "microservice.services.mtd-savings-accounts.port" -> wiremockPort,
    "microservice.services.mtd-crystallisation.host" -> wiremockHost,
    "microservice.services.mtd-crystallisation.port" -> wiremockPort,
    "feature-switch.release-2.enabled" -> false
  )
}
