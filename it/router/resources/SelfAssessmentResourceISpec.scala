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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{ JsObject, Json }
import play.api.libs.ws.{ WSRequest, WSResponse }
import support.IntegrationSpec
import support.stubs.{ AuthStub, DownstreamStub }

class SelfAssessmentResourceISpec extends IntegrationSpec {

  trait Test {
    val nino    = "AA123456B"
    val id      = "SAVKB2UVwUTBQGJ"
    val taxYear = "2017-18"

    val jsonRequest: JsObject  = Json.obj("test" -> "json request")
    val jsonResponse: JsObject = Json.obj("test" -> "json response")

    val locationValue = "test-location-value"

    val acceptHeader: String

    def uri: String           = s"/any/url"
    def downstreamUri: String = s"/any/url"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, acceptHeader))
    }
  }

  "GET /any/url" should {
    "return a 200 with a json response body" when {
      "the downstream response from the self assessment api returns a 200 with a json response body" in new Test {
        override val acceptHeader: String = "application/vnd.hmrc.1.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, jsonResponse, requestHeaders = Map(ACCEPT -> acceptHeader))
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.json shouldBe jsonResponse
      }

      "a version 2.0 header is provided and the downstream response from the self assessment api returns a 200 with a json response body" in new Test {
        override val acceptHeader: String = "application/vnd.hmrc.2.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET,
                                   downstreamUri,
                                   OK,
                                   jsonResponse,
                                   requestHeaders = Map(ACCEPT -> "application/vnd.hmrc.1.0+json"))
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.json shouldBe jsonResponse
      }
    }
  }

  "POST /any/url with a json request body" should {
    "return a 200 with a json response body" when {
      "the downstream response from the self assessment api returns a 200 with a json response body and location header" in new Test {
        override val acceptHeader: String = "application/vnd.hmrc.1.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.POST,
                                   downstreamUri,
                                   OK,
                                   jsonResponse,
                                   requestHeaders = Map(ACCEPT    -> acceptHeader),
                                   responseHeaders = Map(LOCATION -> locationValue))
        }

        val response: WSResponse = await(request.post(jsonRequest))
        response.status shouldBe OK
        response.header(LOCATION) shouldBe Some(locationValue)
        response.json shouldBe jsonResponse
      }
    }
  }

  "PUT /any/url with a json request body" should {
    "return a 200 with a json response body" when {
      "the downstream response from the self assessment api returns a 200 with a json response body" in new Test {
        override val acceptHeader: String = "application/vnd.hmrc.1.0+json"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, OK, jsonResponse, requestHeaders = Map(ACCEPT -> acceptHeader))
        }

        val response: WSResponse = await(request.put(jsonRequest))
        response.status shouldBe OK
        response.json shouldBe jsonResponse
      }
    }
  }
}
