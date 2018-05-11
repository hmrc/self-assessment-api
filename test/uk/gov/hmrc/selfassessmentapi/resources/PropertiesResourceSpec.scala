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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.selfassessmentapi.mocks.connectors.MockPropertiesConnector
import uk.gov.hmrc.selfassessmentapi.models.SourceType
import uk.gov.hmrc.selfassessmentapi.models.properties.NewProperties

import scala.concurrent.Future

class PropertiesResourceSpec extends ResourceSpec
  with MockPropertiesConnector {

  class Setup {
    val resource = new PropertiesResource {
      override val appContext = mockAppContext
      override val authService = mockAuthorisationService
      override val propertiesConnector = mockPropertiesConnector
    }
    mockAPIAction(SourceType.Properties)
  }

  val newProperties = NewProperties()

  "create" should {
    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        val request = FakeRequest().withBody[JsValue](Json.obj())

        MockPropertiesConnector.create(nino, newProperties)
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.create(nino)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }

  "retrieve" should {
    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        MockPropertiesConnector.retrieve(nino)
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.retrieve(nino)(FakeRequest())
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }
}
