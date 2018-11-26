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
import uk.gov.hmrc.r2.selfassessmentapi.mocks.services.MockBanksService
import uk.gov.hmrc.r2.selfassessmentapi.models.SourceType
import uk.gov.hmrc.r2.selfassessmentapi.models.banks.Bank

import scala.concurrent.Future

class BanksResourceSpec extends ResourceSpec
  with MockBanksService {

  class Setup {
    val resource = new BanksResource {
      override val appContext = mockAppContext
      override val authService = mockAuthorisationService
      override val banksService = mockBanksService
    }
    mockAPIAction(SourceType.Banks)
  }

  val sourceId = "test-source-id"
  val accountName = "testAccountName"
  val bank = Bank(None, Some(accountName))
  val bankJson = Jsons.Banks(accountName)

  "create" should {
    "return a 500" when {
      "the future has failed" in new Setup {
        val request = FakeRequest().withBody[JsValue](bankJson)

        MockBanksService.create(nino, bank)
            .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.create(nino)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }

  "update" should {
    "return a 500" when {
      "the future has failed" in new Setup {
        val request = FakeRequest().withBody[JsValue](bankJson)

        MockBanksService.update(nino, bank, sourceId)
            .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.update(nino, sourceId)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }

  "retrieve" should {
    "return a 500" when {
      "the future has failed" in new Setup {
        MockBanksService.retrieve(nino, sourceId)
            .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.retrieve(nino, sourceId)(FakeRequest())
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }

  "retrieveAll" should {
    "return a 500" when {
      "the future has failed" in new Setup {
        MockBanksService.retrieveAll(nino)
            .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.retrieveAll(nino)(FakeRequest())
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }
}
