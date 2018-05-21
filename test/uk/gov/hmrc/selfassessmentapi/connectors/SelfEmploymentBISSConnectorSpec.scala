/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.connectors

import uk.gov.hmrc.http.HttpGet
import uk.gov.hmrc.selfassessmentapi.fixtures.selfemployment.SelfEmploymentBISSFixture
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmploymentBISS

import scala.concurrent.Future

class SelfEmploymentBISSConnectorSpec extends ConnectorSpec {

  class Setup {
    val connector = new SelfEmploymentBISSConnector {
      override val baseUrl: String = desBaseUrl
      override val http: HttpGet = mockHttp
      override val appContext = mockAppContext
    }
    MockAppContext.desToken returns desToken
    MockAppContext.desEnv returns desEnv
  }

  lazy val desBaseUrl = "test-des-url"

  val selfEmploymentBISS = SelfEmploymentBISSFixture.selfEmploymentBISS

  "get" should {

    val url = s"$desBaseUrl/income-store/nino/$nino/self-employments/income-source-summary/${taxYear.toDesTaxYear}"

    "return a SelfEmploymentBISS model" when {
      "des returns a 200 with a correct SelfEmploymentBISS response body" in new Setup {
        MockHttp.GET[Either[Error, SelfEmploymentBISS]](url)
          .returns(Future.successful(Right(selfEmploymentBISS)))

        val result = await(connector.getSummary(nino, taxYear))
        result shouldBe Right(selfEmploymentBISS)
      }
    }
  }
}
