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
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import router.errors.ErrorCode
import support.IntegrationSpec
import support.stubs.{AuthStub, DownstreamStub}

class AuthISpec extends IntegrationSpec {

  trait Test {
    val nino          = "AA123456B"
    val correlationId = "X-123"

    val acceptHeader: String = "application/vnd.hmrc.1.0+json"

    def uri: String           = s"/any/url"
    def downstreamUri: String = s"/any/url"

    val jsonResponse: JsObject = Json.obj("test" -> "json response")

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, acceptHeader))
    }
  }

  "GET /any/url" should {
    "return a success" when {
      "the auth call and downstream call are successful" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, jsonResponse)
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.json shouldBe jsonResponse
      }
    }

    "return UNAUTHORIZED" when {
      "the auth call returns an InvalidBearerToken error" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.unauthorisedOther()
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe FORBIDDEN
        response.json shouldBe ErrorCode.invalidBearerToken.asJson
      }
    }

    "return CLIENT_OR_AGENT_NOT_AUTHORISED" when {
      "the auth call returns an AuthorisationException error" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.unauthorisedNotLoggedIn()
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe FORBIDDEN
        response.json shouldBe ErrorCode.unauthorisedError.asJson
      }
    }
  }

}
