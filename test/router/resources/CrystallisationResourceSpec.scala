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

package router.resources

import mocks.services.MockCrystallisationService
import play.api.libs.json.{JsNull, JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import router.errors.{ErrorCode, IncorrectAPIVersion, UnsupportedAPIVersion}
import support.ResourceSpec
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class CrystallisationResourceSpec extends ResourceSpec
  with MockCrystallisationService {

  class Setup {
    val resource = new CrystallisationResource(
      service = mockCrystallisationService,
      authConnector = mockAuthConnector
    )
    mockAuthAction
  }

  "post" should {
    "return a 204 with response headers" when {
      "the service returns a HttpResponse containing a 204 with no json response body" in new Setup {
        MockCrystallisationService.post(requestJson)
          .returns(Future.successful(Right(HttpResponse(NO_CONTENT, None, testHeaderResponse))))

        private val result = resource.post("", "")(FakeRequest().withBody(requestJson))
        status(result) shouldBe NO_CONTENT
        headers(result) shouldBe testHeader
        contentType(result) shouldBe None
      }
    }


    "return a 406 with a json response body representing the error" when {
      "the service returns an IncorrectAPIVersion response" in new Setup {
        MockCrystallisationService.post(requestJson)
          .returns(Future.successful(Left(IncorrectAPIVersion)))

        private val result = resource.post("", "")(FakeRequest().withBody(requestJson))
        status(result) shouldBe NOT_ACCEPTABLE
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe ErrorCode.invalidAcceptHeader.asJson
      }
    }

    "return a 404 with a json response body representing the error" when {
      "the service returns an UnsupportedAPIVersion response" in new Setup {
        MockCrystallisationService.post(requestJson)
          .returns(Future.successful(Left(UnsupportedAPIVersion)))

        private val result = resource.post("", "")(FakeRequest().withBody(requestJson))
        status(result) shouldBe NOT_FOUND
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe ErrorCode.notFound.asJson
      }
    }
  }

  "intent" when {
    "version 1 header present" when {
      "the service returns a HttpResponse containing a 303 with no json response body" must {
        "return a 303 with response headers" in new Setup {
          MockCrystallisationService.post(requestJson)
            .returns(Future.successful(Right(HttpResponse(SEE_OTHER, None, testHeaderResponse))))

          val result = resource.intent("", "")(
            FakeRequest().withBody(requestJson).withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json"))
          status(result) shouldBe SEE_OTHER
          headers(result) shouldBe testHeader
          contentType(result) shouldBe None
        }
      }


      "return a 406 with a json response body representing the error" when {
        "the service returns an IncorrectAPIVersion response" in new Setup {
          MockCrystallisationService.post(requestJson)
            .returns(Future.successful(Left(IncorrectAPIVersion)))

          val result = resource.intent("", "")(
            FakeRequest().withBody(requestJson).withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json"))
          status(result) shouldBe NOT_ACCEPTABLE
          contentType(result) shouldBe Some(JSON)
          contentAsJson(result) shouldBe ErrorCode.invalidAcceptHeader.asJson
        }
      }

      "return a 404 with a json response body representing the error" when {
        "the service returns an UnsupportedAPIVersion response" in new Setup {
          MockCrystallisationService.post(requestJson)
            .returns(Future.successful(Left(UnsupportedAPIVersion)))

          val result = resource.intent("", "")(
            FakeRequest().withBody(requestJson).withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json"))
          status(result) shouldBe NOT_FOUND
          contentType(result) shouldBe Some(JSON)
          contentAsJson(result) shouldBe ErrorCode.notFound.asJson
        }
      }
    }

    "version 2 header present" when {
      "the service returns a HttpResponse containing a 303 with no json response body" must {
        "return a 303 with response headers" in new Setup {
          MockCrystallisationService.postEmpty
            .returns(Future.successful(Right(HttpResponse(SEE_OTHER, None, testHeaderResponse))))

          val result = resource.intent("", "")()(
            FakeRequest().withBody(JsNull).withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json"))
          status(result) shouldBe SEE_OTHER
          headers(result) shouldBe testHeader
          contentType(result) shouldBe None
        }
      }


      "return a 406 with a json response body representing the error" when {
        "the service returns an IncorrectAPIVersion response" in new Setup {
          MockCrystallisationService.postEmpty
            .returns(Future.successful(Left(IncorrectAPIVersion)))

          val result = resource.intent("", "")()(
            FakeRequest().withBody(JsNull).withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json"))
          status(result) shouldBe NOT_ACCEPTABLE
          contentType(result) shouldBe Some(JSON)
          contentAsJson(result) shouldBe ErrorCode.invalidAcceptHeader.asJson
        }
      }

      "return a 404 with a json response body representing the error" when {
        "the service returns an UnsupportedAPIVersion response" in new Setup {
          MockCrystallisationService.postEmpty
            .returns(Future.successful(Left(UnsupportedAPIVersion)))

          val result = resource.intent("", "")()(
            FakeRequest().withBody(JsNull).withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json"))
          status(result) shouldBe NOT_FOUND
          contentType(result) shouldBe Some(JSON)
          contentAsJson(result) shouldBe ErrorCode.notFound.asJson
        }
      }
    }

    "no version header present" when {
      "return a 406 with a json response body representing the error" when {
        "the service returns an IncorrectAPIVersion response" in new Setup {
          MockCrystallisationService.post(requestJson)
            .returns(Future.successful(Left(IncorrectAPIVersion)))

          val result = resource.intent("", "")(FakeRequest().withBody(requestJson))
          status(result) shouldBe NOT_ACCEPTABLE
          contentType(result) shouldBe Some(JSON)
          contentAsJson(result) shouldBe ErrorCode.invalidAcceptHeader.asJson
        }
      }

      "return a 404 with a json response body representing the error" when {
        "the service returns an UnsupportedAPIVersion response" in new Setup {
          MockCrystallisationService.post(requestJson)
            .returns(Future.successful(Left(UnsupportedAPIVersion)))

          val result = resource.intent("", "")(FakeRequest().withBody(requestJson))
          status(result) shouldBe NOT_FOUND
          contentType(result) shouldBe Some(JSON)
          contentAsJson(result) shouldBe ErrorCode.notFound.asJson
        }
      }
    }
  }

  "retrieve" should {

    val body: JsValue = Json.parse(
      """
        |{
        |  "obligations": [
        |    {
        |    	"identification": {
        |				"incomeSourceType": "ITSA",
        |				"referenceNumber": "AB123456A",
        |				"referenceType": "NINO"
        |			},
        |    "obligationDetails": [
        |      {
        |        "status": "O",
        |        "inboundCorrespondenceFromDate": "2018-02-01",
        |        "inboundCorrespondenceToDate": "2018-02-28",
        |        "inboundCorrespondenceDateReceived": "2018-04-01",
        |        "inboundCorrespondenceDueDate": "2018-05-28"
        |      }
        |    ]
        |    }
        |  ]
        |}
      """.stripMargin)


    "return a response with status 200, with a header body containing an obligations" when {
      "the service returns a 200 HttpResponse containing a 200 and a response body" in new Setup {

        val httpResponse = HttpResponse(OK, Some(body), testHeaderResponse)
        MockCrystallisationService.get()
          .returns(Future.successful(Right(httpResponse)))

        private val result = resource.get()(FakeRequest())
        status(result) shouldBe OK
        headers(result) shouldBe testHeader
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe body
      }

      "return a 406 with a json response body representing the error" when {
        "the service returns an IncorrectAPIVersion response" in new Setup {
          MockCrystallisationService.get()
            .returns(Future.successful(Left(IncorrectAPIVersion)))

          val result: Future[Result] = resource.get("")(FakeRequest())
          status(result) shouldBe NOT_ACCEPTABLE
          contentType(result) shouldBe Some(JSON)
          contentAsJson(result) shouldBe ErrorCode.invalidAcceptHeader.asJson
        }
      }

      "return a 404 with a json response body representing the error" when {
        "the service returns an UnsupportedAPIVersion response" in new Setup {
          MockCrystallisationService.get()
            .returns(Future.successful(Left(UnsupportedAPIVersion)))

          val result: Future[Result] = resource.get("")(FakeRequest())
          status(result) shouldBe NOT_FOUND
          contentType(result) shouldBe Some(JSON)
          contentAsJson(result) shouldBe ErrorCode.notFound.asJson
        }
      }

    }
  }

}
