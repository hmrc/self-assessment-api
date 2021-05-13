/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.mvc.Result
import play.api.test.FakeRequest
import router.errors.{ErrorCode, IncorrectAPIVersion, UnsupportedAPIVersion}
import support.ResourceSpec
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaxCalcResourceSpec extends ResourceSpec {

  class Setup {
    val resource = new TaxCalcResource(
      authConnector = mockAuthConnector,
      cc = controllerComponents
    )
    mockAuthAction
  }

  "get" should {
    "return a 200 with the response headers" when {
      "the service returns a HttpResponse containing a 200 with no json response body" in new Setup {

        val result: Future[Result] = resource.get("","")(FakeRequest())
        status(result) shouldBe GONE
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe ErrorCode.resourceGone.asJson
      }
    }

    "return a 200 with a json response body and response headers" when {
      "the service returns a HttpResponse containing a 200 with a json response body" in new Setup {

        val result: Future[Result] = resource.get("","")(FakeRequest())
        status(result) shouldBe GONE
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe ErrorCode.resourceGone.asJson
      }
    }

    "return a 406 with a json response body representing the error" when {
      "the service returns an IncorrectAPIVersion response" in new Setup {

        val result: Future[Result] = resource.get("","")(FakeRequest())
        status(result) shouldBe GONE
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe ErrorCode.resourceGone.asJson
      }
    }

    "return a 404 with a json response body representing the error" when {
      "the service returns an UnsupportedAPIVersion response" in new Setup {

        val result: Future[Result] = resource.get("","")(FakeRequest())
        status(result) shouldBe GONE
        contentType(result) shouldBe Some(JSON)
        contentAsJson(result) shouldBe ErrorCode.resourceGone.asJson
      }
    }
  }

}
