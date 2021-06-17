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

class PropertyEopsObligationsResourceISpec extends IntegrationSpec {

  trait Test {
    val nino    = "AA123456B"

    val jsonResponse: JsObject = Json.obj("test" -> "json response")

    val acceptHeader: String = "application/vnd.hmrc.2.0+json"

    def uri: String           = s"/ni/$nino/uk-properties/end-of-period-statements/obligations?from=2017-04-06&to=2018-04-05"
    def downstreamUri: String = s"/2.0/ni/$nino/uk-properties/end-of-period-statements/obligations"

    val queryParams: Map[String, String] = Map("from" -> "2017-04-06", "to" -> "2018-04-05")

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, acceptHeader))
    }
  }

  "GET Property End of Period Statements" should {
    "return a 200 with a json response body" when {
      "the downstream response from UK Property version 2.0 returns a 200 with a json response body" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, jsonResponse, queryParams = queryParams, requestHeaders = Map(ACCEPT -> acceptHeader))
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.json shouldBe jsonResponse
      }
    }
  }
}
