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

package router.connectors


import mocks.MockHttp
import mocks.config.MockAppConfig
import mocks.httpParser.MockSelfAssessmentHttpParser
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import router.httpParsers.SelfAssessmentHttpParser.SelfAssessmentOutcome
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class CrystallisationConnectorSpec extends UnitSpec
  with MockHttp
  with MockAppConfig
  with MockSelfAssessmentHttpParser {


  class Setup {

    object TestConnector extends CrystallisationConnector(
      mockHttp,
      mockSelfAssessmentHttpParser,
      mockAppConfig
    )

    MockAppConfig.crystallisationApiUrl returns crystallisationApiUrl
  }

  lazy val crystallisationApiUrl = "test-di-api-url"
  val path = "/2.0/test-path"

  "post" should {
    "return a HttpResponse" when {
      "a successful HttpResponse is returned" in new Setup {
        val request = FakeRequest("POST", path)
        val response = HttpResponse(Status.NO_CONTENT)
        val requestJson = Json.obj("test" -> "request json")

        MockSelfAssessmentHttpParser.read.returns(Right(response))
        MockHttp.POST[JsValue, SelfAssessmentOutcome](s"$crystallisationApiUrl$path", requestJson)
          .returns(Future.successful(Right(response)))
        await(TestConnector.post(path, requestJson)(hc, request)) shouldBe Right(response)
      }
    }
  }

  "postEmpty" should {
    "return a HttpResponse" when {
      "a successful HttpResponse is returned" in new Setup {
        val request = FakeRequest("POST", path)
        val response = HttpResponse(Status.NO_CONTENT)

        MockSelfAssessmentHttpParser.read.returns(Right(response))
        MockHttp.POSTEmpty[SelfAssessmentOutcome](s"$crystallisationApiUrl$path")
          .returns(Future.successful(Right(response)))
        await(TestConnector.postEmpty(path)(hc, request)) shouldBe Right(response)
      }
    }
  }

  "retrieve" should {
    "return a HttpResponse" when {
      "a successful HttpResponse is returned" in new Setup {
        val request = FakeRequest("GET", path)
        val response  = HttpResponse(Status.OK, Some(Json.obj()))

        MockSelfAssessmentHttpParser.read.returns(Right(response))
        MockHttp.GET[SelfAssessmentOutcome](s"$crystallisationApiUrl$path")
          .returns(Future.successful(Right(response)))
        await(TestConnector.get(path)(hc, request)) shouldBe Right(response)
      }
    }
  }
}

