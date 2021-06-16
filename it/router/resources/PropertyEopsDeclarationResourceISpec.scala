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
import support.IntegrationSpec
import support.stubs.{AuthStub, DownstreamStub}

class PropertyEopsDeclarationResourceISpec extends IntegrationSpec {

  trait Test {
    val nino    = "AA123456B"

    val jsonRequest: JsObject = Json.obj("test" -> "json request")
    val jsonResponse: JsObject = Json.obj("test" -> "json response")

    val acceptHeader: String = "application/vnd.hmrc.2.0+json"

    def uri: String           = s"/ni/$nino/uk-properties/end-of-period-statements/from/2018-01-01/to/2018-12-12"
    def downstreamUri: String = s"/2.0/ni/$nino/uk-properties/end-of-period-statements/from/2018-01-01/to/2018-12-12"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, acceptHeader))
    }
  }

  "Post UK Property EOPS declaration" should {
    "return a 204 with no json response body" when {
      "the downstream response from the UK property api version 2.0 returns a 204 with no json response body" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          //            MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, NO_CONTENT, jsonResponse)
        }

        val response: WSResponse = await(request.post(jsonRequest))
        response.status shouldBe SEE_OTHER
        response.header(ACCEPT) shouldBe Some("application/vnd.hmrc.2.0+json")
      }
    }
  }
}
