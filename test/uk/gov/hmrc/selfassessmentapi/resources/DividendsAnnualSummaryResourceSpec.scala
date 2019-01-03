/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import uk.gov.hmrc.selfassessmentapi.mocks.services.MockDividendsAnnualSummaryService
import uk.gov.hmrc.selfassessmentapi.models.SourceType
import uk.gov.hmrc.selfassessmentapi.models.dividends.Dividends

import scala.concurrent.Future

class DividendsAnnualSummaryResourceSpec extends ResourceSpec
  with MockDividendsAnnualSummaryService {

  class Setup {
    val resource = new DividendsAnnualSummaryResource {
      override val appContext = mockAppContext
      override val dividendsService = mockDividendsAnnualSummaryService
      override val authService = mockAuthorisationService
    }
    mockAPIAction(SourceType.Dividends)
  }

  val ukDividends: BigDecimal = 750.23
  val dividends = Dividends(Some(ukDividends))
  val dividendsJson = Jsons.Dividends(ukDividends)

  "updateAnnualSummary" should {
    "return a 500" when {
      "the service returns a failed future" in new Setup {
        val request = FakeRequest().withBody[JsValue](dividendsJson)

        MockDividendsAnnualSummaryService.updateAnnualSummary(nino, taxYear, dividends)
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.updateAnnualSummary(nino, taxYear)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }

  "retrieveAnnualSummary" should {
    "return a 500" when {
      "the service returns a failed future" in new Setup {
        MockDividendsAnnualSummaryService.retrieveAnnualSummary(nino, taxYear)
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.retrieveAnnualSummary(nino, taxYear)(FakeRequest())
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }
}
