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

class PropertiesObligationsResourceSpec extends ResourceSpec
  with MockObligationsConnector {

  class Setup {
    val resource = new PropertiesObligationsResource {
      override val appContext = mockAppContext
      override val authService = mockAuthorisationService
      override val connector = mockObligationsConnector
    }
    mockAPIAction(SourceType.Properties)
  }

  "retrieveObligations" should {
    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        MockObligationsConnector.get(nino, "ITSP")
          .returns(Future.failed(new RuntimeException("somemthing went wrong")))

        val result = resource.retrieveObligations(nino)(FakeRequest())
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }
}
