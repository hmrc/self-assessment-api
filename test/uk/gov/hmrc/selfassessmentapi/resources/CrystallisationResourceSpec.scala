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
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.mocks.connectors.MockCrystallisationConnector
import uk.gov.hmrc.selfassessmentapi.mocks.services.MockAuditService
import uk.gov.hmrc.selfassessmentapi.models.SourceType
import uk.gov.hmrc.selfassessmentapi.models.crystallisation.CrystallisationRequest
import uk.gov.hmrc.selfassessmentapi.resources.utils.ObligationQueryParams
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.CrystallisationIntentResponse

import scala.concurrent.Future

class CrystallisationResourceSpec extends ResourceSpec
  with MockCrystallisationConnector with MockAuditService {

  class Setup {
    val resource = new CrystallisationResource(
      mockCrystallisationConnector,
      mockAppContext,
      mockAuthorisationService,
      // TODO What needs mocking on this?
      mockAuditService
    )
    //      override val appContext = mockAppContext
    //      override val authService = mockAuthorisationService
    //      override val crystallisationConnector = mockCrystallisationConnector
    //    }
    mockAPIAction(SourceType.Crystallisation)
  }

  val calculationId = "test-calc-id"
  val crystallisationRequest = CrystallisationRequest(calculationId)
  val crystallisationRequestJson = Jsons.Crystallisation.crystallisationRequest(calculationId)

  val intentToCrystalliseJson = Jsons.Crystallisation.intentToCrystallise()
  val crystallisationIntentResponse = CrystallisationIntentResponse(HttpResponse(OK, Some(intentToCrystalliseJson)))

  val requestTimestamp = "test-timestamp"
  val obligationQueryParams = ObligationQueryParams(None, None, Some("O"))

  "intentToCrystallise" should {
    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        val request = FakeRequest().withHeaders("X-Request-Timestamp" -> requestTimestamp)

        MockCrystallisationConnector.intentToCrystallise(nino, taxYear, requestTimestamp)
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.intentToCrystallise(nino, taxYear)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }

  "crystallisation" should {
    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        val request = FakeRequest()
          .withHeaders("X-Request-Timestamp" -> requestTimestamp)
          .withBody[JsValue](crystallisationRequestJson)

        MockCrystallisationConnector.crystallise(nino, taxYear, crystallisationRequest)
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.crystallisation(nino, taxYear)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }

  "retrieveObligation" should {
    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        val newParams = ObligationQueryParams(Some(taxYear.taxYearFromDate), Some(taxYear.taxYearToDate), Some("O"))

        MockCrystallisationConnector.get(nino, newParams)
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.retrieveObligation(nino, taxYear, obligationQueryParams)(FakeRequest())
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }
}
