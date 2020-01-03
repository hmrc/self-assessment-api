/*
 * Copyright 2020 HM Revenue & Customs
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

import mocks.services.MockSavingsAccountService
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import router.errors.{ErrorCode, IncorrectAPIVersion, UnsupportedAPIVersion}
import support.ResourceSpec
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SavingsAccountResourceSpec extends ResourceSpec
  with MockSavingsAccountService {

  class Setup {
    val resource = new SavingsAccountResource(
      service = mockSavingsAccountService,
      authConnector = mockAuthConnector
    )
    mockAuthAction
  }

  val request: JsObject = Json.obj("accountName" -> "Main account name")

  "post" should {
    "return status 201 with the response body and headers" when {
      "the service returns a HttpResponse containing status 201 with body" in new Setup {
        val response: JsObject = Json.obj("id" -> "SAVKB2UVwUTBQGJ")

        MockSavingsAccountService.post(request)
          .returns(Future.successful(Right(HttpResponse(CREATED, Some(response), testHeaderResponse))))

        private val result = resource.post("nino", "taxYear")(FakeRequest().withBody(request))
        status(result) shouldBe CREATED
        headers(result) shouldBe testHeader
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe response
      }
    }

    "return error response body with status 406" when {
      "the service returns an IncorrectAPIVersion response" in new Setup {
        MockSavingsAccountService.post(request)
          .returns(Future.successful(Left(IncorrectAPIVersion)))

        val result: Future[Result] = resource.post("nino", "taxYear")(FakeRequest().withBody(request))
        status(result) shouldBe NOT_ACCEPTABLE
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe ErrorCode.invalidAcceptHeader.asJson
      }
    }

    "return error response body with status 404" when {
      "the service returns an UnsupportedAPIVersion response" in new Setup {
        MockSavingsAccountService.post(request)
          .returns(Future.successful(Left(UnsupportedAPIVersion)))

        val result: Future[Result] = resource.post("nino", "taxYear")(FakeRequest().withBody(request))
        status(result) shouldBe NOT_FOUND
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe ErrorCode.notFound.asJson
      }
    }
  }

  "get" should {
    "return a 200 with the response body and headers" when {
      "the service returns a HttpResponse containing a 200 and response body" in new Setup {

        val body: JsValue = Json.parse(
          s"""{
             |    "savingsAccounts": [
             |        {
             |            "id": "SAVKB2UVwUTBQGJ",
             |            "accountName": "Main account name"
             |        },
             |        {
             |            "id": "SAVKB2UVwUTBQGK",
             |            "accountName": "Shares savings account"
             |        }
             |    ]
             |}""".stripMargin
        )

        val httpResponse = HttpResponse(OK, Some(body), testHeaderResponse)

        MockSavingsAccountService.get()
          .returns(Future.successful(Right(httpResponse)))

        private val result = resource.get("")(FakeRequest())
        status(result) shouldBe OK
        headers(result) shouldBe testHeader
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe body
      }
    }

    "return a 406 with a json response body representing the error" when {
      "the service returns an IncorrectAPIVersion response" in new Setup {
        MockSavingsAccountService.get()
          .returns(Future.successful(Left(IncorrectAPIVersion)))

        val result: Future[Result] = resource.get("")(FakeRequest())
        status(result) shouldBe NOT_ACCEPTABLE
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe ErrorCode.invalidAcceptHeader.asJson
      }
    }

    "return a 404 with a json response body representing the error" when {
      "the service returns an UnsupportedAPIVersion response" in new Setup {
        MockSavingsAccountService.get()
          .returns(Future.successful(Left(UnsupportedAPIVersion)))

        val result: Future[Result] = resource.get("")(FakeRequest())
        status(result) shouldBe NOT_FOUND
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe ErrorCode.notFound.asJson
      }
    }
  }

  "amend" should {
    "return a 204 with the response headers" when {
      "the service returns a HttpResponse containing a 204 with no json response body" in new Setup {
        MockSavingsAccountService.put(requestJson)
          .returns(Future.successful(Right(HttpResponse(NO_CONTENT, None, testHeaderResponse))))

        private val result = resource.put("", "")(FakeRequest().withBody(requestJson))
        status(result) shouldBe NO_CONTENT
        headers(result) shouldBe testHeader
        contentType(result) shouldBe None
      }
    }

    "return a 406 with a json response body representing the error" when {
      "the service returns an IncorrectAPIVersion response" in new Setup {
        MockSavingsAccountService.put(requestJson).returns(Future.successful(Left(IncorrectAPIVersion)))

        val result = resource.put("", "")(FakeRequest().withBody(requestJson))
        status(result) shouldBe NOT_ACCEPTABLE
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe ErrorCode.invalidAcceptHeader.asJson
      }
    }

    "return a 404 with a json response body representing the error" when {
      "the service returns an UnsupportedAPIVersion response" in new Setup {
        MockSavingsAccountService.put(requestJson).returns(Future.successful(Left(UnsupportedAPIVersion)))

        val result = resource.put("", "")(FakeRequest().withBody(requestJson))
        status(result) shouldBe NOT_FOUND
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe ErrorCode.notFound.asJson
      }
    }
  }

}
