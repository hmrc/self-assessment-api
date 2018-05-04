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

import org.scalatestplus.play.OneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.selfassessmentapi.mocks.services.MockBanksAnnualSummaryService
import uk.gov.hmrc.selfassessmentapi.models.banks.BankAnnualSummary
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceType}

import scala.concurrent.Future

class BanksAnnualSummaryResourceSpec extends ResourceSpec with OneAppPerSuite
  with MockBanksAnnualSummaryService {

  class Setup {
    val resource = new BanksAnnualSummaryResource {
      val annualSummaryService = mockBanksAnnualSummaryService
      val appContext = mockAppContext
      val authService = mockAuthorisationService
    }
    mockAPIAction(SourceType.Banks)
  }

  class SetupWithAuthEnabled extends Setup {
    mockAPIAction(SourceType.Banks, authEnabled = true)
  }

  val nino = generateNino
  val sourceId = "test-sourceId"
  val taxedUkInterest = Some(BigDecimal(50.30))
  val untaxedUkInterest = Some(BigDecimal(70.15))

  val emptyBankSummary = BankAnnualSummary(None, None)

  val bankAnnualSummary = BankAnnualSummary(taxedUkInterest, untaxedUkInterest)
  val bankAnnualSummaryJson = Jsons.Banks.annualSummary(taxedUkInterest, untaxedUkInterest)

  "updateAnnualSummary" should {
    "return a 204" when {
      "the request contains an empty body" in new Setup {
        val request: FakeRequest[JsValue] = FakeRequest().withBody[JsValue](Json.obj())

        MockBanksAnnualSummaryService
          .updateAnnualSummary(nino, sourceId, taxYear, emptyBankSummary)
          .returns(Future.successful(true))

        val result = resource.updateAnnualSummary(nino, sourceId, taxYear)(request)
        status(result) shouldBe NO_CONTENT
        contentType(result) shouldBe None
      }

      "the request contains the correct body" in new Setup {
        val request: FakeRequest[JsValue] = FakeRequest().withBody[JsValue](bankAnnualSummaryJson)

        MockBanksAnnualSummaryService
          .updateAnnualSummary(nino, sourceId, taxYear, bankAnnualSummary)
          .returns(Future.successful(true))

        val result = resource.updateAnnualSummary(nino, sourceId, taxYear)(request)
        status(result) shouldBe NO_CONTENT
        contentType(result) shouldBe None
      }
    }

    "return a 400" when {
      "the request contains a incorrect value for taxedUkInterest" in new Setup {
        val incorrectBankAnnualSummaryJson = Json.obj("taxedUkInterest" -> "body")
        val request: FakeRequest[JsValue] = FakeRequest().withBody[JsValue](incorrectBankAnnualSummaryJson)

        MockBanksAnnualSummaryService
          .updateAnnualSummary(nino, sourceId, taxYear, emptyBankSummary)
          .returns(Future.successful(true))

        val result = resource.updateAnnualSummary(nino, sourceId, taxYear)(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result) shouldBe Some(MimeTypes.JSON)

        val error = Errors.numberFormatExceptionError("/taxedUkInterest")
        contentAsJson(result) shouldBe Json.toJson(Errors.badRequest(error))
      }

//      todo: Uncomment as part of MTSDA-1673 after fix
//      "the request is authorised as a filing only agent" in new SetupWithAuthEnabled {
//        val request: FakeRequest[JsValue] = FakeRequest().withBody[JsValue](Json.obj())
//        val filingOnlyAgent = FilingOnlyAgent(Some("test-agent-code"), None)
//
//        MockAuthorisationService.authCheck(nino)
//          .returns(Future.successful(Right(filingOnlyAgent)))
//
//        MockBanksAnnualSummaryService
//          .updateAnnualSummary(nino, sourceId, taxYear, newBankSummary)
//          .returns(Future.successful(false))
//
//        val result = resource.updateAnnualSummary(nino, sourceId, taxYear)(request)
//        status(result) shouldBe BAD_REQUEST
//      }
    }

    "return a 404" when {
      "the request body is correct but the update returns false" in new Setup {
        val request: FakeRequest[JsValue] = FakeRequest().withBody[JsValue](bankAnnualSummaryJson)

        MockBanksAnnualSummaryService
          .updateAnnualSummary(nino, sourceId, taxYear, bankAnnualSummary)
          .returns(Future.successful(false))

        val result = resource.updateAnnualSummary(nino, sourceId, taxYear)(request)
        status(result) shouldBe NOT_FOUND
        contentType(result) shouldBe None
      }
    }
  }
}
