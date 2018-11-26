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

package uk.gov.hmrc.r2.selfassessmentapi.resources

import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.mocks.connectors.MockCharitableGivingsConnector
import uk.gov.hmrc.selfassessmentapi.models.SourceType
import uk.gov.hmrc.selfassessmentapi.models.des.charitablegiving.{CharitableGivings, GiftAidPayments, Gifts}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.{CharitableGivingsResponse, EmptyResponse}

import scala.concurrent.Future

class CharitableGivingsResourceSpec extends BaseResourceSpec
  with MockCharitableGivingsConnector {

  class Setup {
    val resource = new CharitableGivingsResource {
      override val charitableGivingsConnector = mockCharitableGivingsConnector
      override val appContext = mockAppContext
      override val authService = mockAuthorisationService
    }
    mockAPIAction(SourceType.CharitableGivings)
  }

  val giftAidPayments = GiftAidPayments(
    currentYear = Some(10000.32),
    oneOffCurrentYear = Some(1000.23),
    currentYearTreatedAsPreviousYear = Some(300.27),
    nextYearTreatedAsCurrentYear = Some(400.13),
    nonUKCharities = Some(2000.19)
  )
  val gifts = Gifts(
    landAndBuildings = Some(700.11),
    sharesOrSecurities = Some(600.31),
    investmentsNonUKCharities = Some(300.22)
  )
  val charitableGivings = CharitableGivings(Some(giftAidPayments), Some(gifts))
  val emptyCharitableGivings = CharitableGivings(None, None)
  val charitableGivingsJson = Jsons.CharitableGivings.apply()

  "updatePayments" should {
    "return a 204" when {
      "the request body is correct and DES return a 204" in new Setup {
        val request = FakeRequest().withBody[JsValue](charitableGivingsJson)

        MockCharitableGivingsConnector.update(nino, taxYear, charitableGivings)
          .returns(Future.successful(EmptyResponse(HttpResponse(NO_CONTENT))))

        val result = resource.updatePayments(nino, taxYear)(request)
        status(result) shouldBe NO_CONTENT
        contentType(result) shouldBe None
      }

      "the request body is empty and DES return a 204" in new Setup {
        val request = FakeRequest().withBody[JsValue](Json.obj())

        MockCharitableGivingsConnector.update(nino, taxYear, emptyCharitableGivings)
          .returns(Future.successful(EmptyResponse(HttpResponse(NO_CONTENT))))

        val result = resource.updatePayments(nino, taxYear)(request)
        status(result) shouldBe NO_CONTENT
        contentType(result) shouldBe None
      }
    }

    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        val request = FakeRequest().withBody[JsValue](charitableGivingsJson)

        MockCharitableGivingsConnector.update(nino, taxYear, charitableGivings)
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.updatePayments(nino, taxYear)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }

  "retrievePayments" should {
    "return a 200 with a json response body" when {

      val desResponse = CharitableGivingsResponse(HttpResponse(OK, Some(charitableGivingsJson)))

      "DES returns a 200 with a charitable payments json response body" in new Setup {
        MockCharitableGivingsConnector.get(nino, taxYear)
          .returns(Future.successful(desResponse))

        val result = resource.retrievePayments(nino, taxYear)(FakeRequest())
        status(result) shouldBe OK
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe charitableGivingsJson
      }
    }

    "return a 400" when {

      val desInvalidTaxYearResponse = CharitableGivingsResponse(HttpResponse(BAD_REQUEST, Some(Json.toJson(Jsons.Errors.invalidTaxYear))))

      "DES returns a 400 because of an invalid tax year" in new Setup {
        MockCharitableGivingsConnector.get(nino, taxYear)
          .returns(Future.successful(desInvalidTaxYearResponse))

        val result = resource.retrievePayments(nino, taxYear)(FakeRequest())
        status(result) shouldBe BAD_REQUEST
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe Json.toJson(Jsons.Errors.invalidTaxYear)
      }
    }

    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        MockCharitableGivingsConnector.get(nino, taxYear)
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.retrievePayments(nino, taxYear)(FakeRequest())
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }
}
