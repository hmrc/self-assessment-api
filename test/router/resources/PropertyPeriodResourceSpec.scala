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

import mocks.services.MockPropertyPeriodService
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import router.errors.InvalidRequest
import support.ResourceSpec
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class PropertyPeriodResourceSpec extends ResourceSpec
  with MockPropertyPeriodService {

  class Setup {
    val resource = new PropertyPeriodResource(
      service = mockPropertyPeriodService,
      authConnector = mockAuthConnector
    )
    mockAuthAction
  }

  val request = FakeRequest()
  val requestJson: JsValue = Json.obj("test" -> "request json")
  val responseJson: JsValue = Json.obj("test" -> "response json")
  val testHeader = Map("test" -> "header",  "X-Content-Type-Options" -> "nosniff")
  val testHeaderResponse = Map("test" -> Seq("header"))

  "create" should {
    "return a 201 with the response headers" when {
      "the service returns a HttpResponse containing a 201 with no json response body" in new Setup {
        MockPropertyPeriodService.create()
          .returns(Future.successful(Right(HttpResponse(CREATED, None, testHeaderResponse))))

        private val result = resource.create("")(FakeRequest().withBody(requestJson))
        status(result) shouldBe CREATED
        headers(result) shouldBe testHeader
        contentType(result) shouldBe None
      }
    }
  }

}
