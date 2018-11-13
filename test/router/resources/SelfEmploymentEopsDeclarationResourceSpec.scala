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

package router.resources

import mocks.services.MockSelfEmploymentEopsDeclarationService
import play.api.libs.json.Json
import play.api.test.FakeRequest
import router.errors.{ErrorCode, IncorrectAPIVersion, UnsupportedAPIVersion}
import support.ResourceSpec
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class SelfEmploymentEopsDeclarationResourceSpec extends ResourceSpec
  with MockSelfEmploymentEopsDeclarationService {

  class Setup {
    val resource = new SelfEmploymentEopsDeclarationResource(
      service = mockSelfEmploymentEopsDeclarationService,
      authConnector = mockAuthConnector
    )
    mockAuthAction
  }

  val request = FakeRequest()
  val requestJson = Json.obj("test" -> "request json")
  val responseJson = Json.obj("test" -> "response json")
  val testHeader = Map("test" -> "header",  "X-Content-Type-Options" -> "nosniff")
  val testHeaderResponse = Map("test" -> Seq("header"))

  "post" should {
    "return a 204 with the response headers" when {
      "the service returns a HttpResponse containing a 204 with no json response body" in new Setup {
        MockSelfEmploymentEopsDeclarationService.post()
          .returns(Future.successful(Right(HttpResponse(NO_CONTENT, None, testHeaderResponse))))

        val result = resource.post("","","","")(FakeRequest().withBody(requestJson))
        status(result) shouldBe NO_CONTENT
        headers(result) shouldBe testHeader
        contentType(result) shouldBe None
      }
    }

    "return a 200 with a json response body and response headers" when {
      "the service returns a HttpResponse containing a 200 with a json response body" in new Setup {
        MockSelfEmploymentEopsDeclarationService.post()
          .returns(Future.successful(Right(HttpResponse(OK, Some(responseJson), testHeaderResponse))))

        val result = resource.post("","","","")(FakeRequest().withBody(requestJson))
        status(result) shouldBe OK
        headers(result) shouldBe testHeader
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe responseJson
      }
    }

    "return a 406 with a json response body representing the error" when {
      "the service returns an IncorrectAPIVersion response" in new Setup {
        MockSelfEmploymentEopsDeclarationService.post()
          .returns(Future.successful(Left(IncorrectAPIVersion)))

        val result = resource.post("","","","")(FakeRequest().withBody(requestJson))
        status(result) shouldBe NOT_ACCEPTABLE
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe ErrorCode.invalidAcceptHeader.asJson
      }
    }

    "return a 404 with a json response body representing the error" when {
      "the service returns an UnsupportedAPIVersion response" in new Setup {
        MockSelfEmploymentEopsDeclarationService.post()
          .returns(Future.successful(Left(UnsupportedAPIVersion)))

        val result = resource.post("","","","")(FakeRequest().withBody(requestJson))
        status(result) shouldBe NOT_FOUND
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe ErrorCode.notFound.asJson
      }
    }
  }

}
