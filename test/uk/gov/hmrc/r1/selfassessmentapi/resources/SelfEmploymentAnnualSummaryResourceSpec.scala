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

package uk.gov.hmrc.r1.selfassessmentapi.resources

import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import uk.gov.hmrc.r2.selfassessmentapi.fixtures.selfemployment
import uk.gov.hmrc.r2.selfassessmentapi.mocks.connectors.MockSelfEmploymentAnnualSummaryConnector
import uk.gov.hmrc.r2.selfassessmentapi.models.SourceType

import scala.concurrent.Future

class SelfEmploymentAnnualSummaryResourceSpec extends ResourceSpec
  with MockSelfEmploymentAnnualSummaryConnector {

  class Setup {
    val resource = new SelfEmploymentAnnualSummaryResource {
      override val appContext = mockAppContext
      override val authService = mockAuthorisationService
      override val connector = mockSelfEmploymentAnnualSummaryConnector
    }
    mockAPIAction(SourceType.SelfEmployments)
  }

  val sourceId = "test-source-id"
  val desAnnualSummary = selfemployment.des.AnnualSummaryFixture.annualSummary()
  val annualSummaryJson = Jsons.SelfEmployment.annualSummary()

  "updateAnnualSummary" should {
    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        val request = FakeRequest().withBody[JsValue](annualSummaryJson)

        MockSelfEmploymentAnnualSummaryConnector.update(nino, sourceId, taxYear, desAnnualSummary)
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.updateAnnualSummary(nino, sourceId, taxYear)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }

  "retrieveAnnualSummary" should {
    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        MockSelfEmploymentAnnualSummaryConnector.get(nino, sourceId, taxYear)
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.retrieveAnnualSummary(nino, sourceId, taxYear)(FakeRequest())
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }
}
