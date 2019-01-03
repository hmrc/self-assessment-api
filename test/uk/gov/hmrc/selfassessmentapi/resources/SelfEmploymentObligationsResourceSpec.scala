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

import play.api.test.FakeRequest
import uk.gov.hmrc.selfassessmentapi.mocks.connectors.MockObligationsConnector
import uk.gov.hmrc.selfassessmentapi.models.SourceType

import scala.concurrent.Future

class SelfEmploymentObligationsResourceSpec extends ResourceSpec
  with MockObligationsConnector {

  class Setup {
    val resource = new SelfEmploymentObligationsResource {
      override val appContext = mockAppContext
      override val authService = mockAuthorisationService
      override val obligationsConnector = mockObligationsConnector
    }
    mockAPIAction(SourceType.SelfEmployments)
  }

  val sourceId = "test-source-id"

  "retrieveObligations" should {
    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        val request = FakeRequest().ignoreBody

        MockObligationsConnector.get(nino, "ITSB")
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.retrieveObligations(nino, sourceId)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
