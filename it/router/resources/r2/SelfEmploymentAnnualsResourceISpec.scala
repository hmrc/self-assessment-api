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

package router.resources.r2

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.ReleaseTwoIntegrationSpec
import support.stubs.{AuthStub, DownstreamStub}

class SelfEmploymentAnnualsResourceISpec extends ReleaseTwoIntegrationSpec {

  val selfEmploymentId = "test-selfemployment-id"

  trait Test {
    val nino = "AA123456B"

    val acceptHeader: String

    def uri: String           = s"/ni/$nino/self-employments/$selfEmploymentId/2018-19"
    def downstreamUri: String = s"/r2/ni/$nino/self-employments/$selfEmploymentId/2018-19"

    val jsonRequest: JsObject = Json.obj("test" -> "json request")
    val jsonResponse: JsObject = Json.obj("test" -> "json response")

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, acceptHeader))
    }
  }

  "GET Self Employment annuals with release-2 enabled" should {

      s"return a 200 with a json response body" when {
        "the downstream response from the self assessment api release 2 returns returns a 200 with a json response body" in new Test {
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
            DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, jsonResponse, requestHeaders = Map(ACCEPT -> "application/vnd.hmrc.1.0+json"))
          }

          val response: WSResponse = await(request.get)
          response.status shouldBe OK
          response.json shouldBe jsonResponse
        }
      }
  }

  "PUT Self Employment annuals with release-2 enabled" should {

      s"return a 204 with no json response body for $selfEmploymentId properties" when {
        "a version 1.0 header is provided and the downstream response from the self assessment api returns a 204 with a json response body" in new Test {
          override val acceptHeader: String = "application/vnd.hmrc.1.0+json"

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, jsonResponse, requestHeaders = Map(ACCEPT -> acceptHeader))
          }

          val response: WSResponse = await(request.put(jsonRequest))
          response.status shouldBe NO_CONTENT
        }

        "a version 2.0 header is provided and the downstream response from the self assessment api returns a 204 with a json response body" in new Test {
          override val acceptHeader: String = "application/vnd.hmrc.2.0+json"

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, jsonResponse, requestHeaders = Map(ACCEPT -> "application/vnd.hmrc.1.0+json"))
          }

          val response: WSResponse = await(request.put(jsonRequest))
          response.status shouldBe NO_CONTENT
        }
      }
  }
}
