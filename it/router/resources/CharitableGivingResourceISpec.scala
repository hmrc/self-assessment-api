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

class CharitableGivingResourceISpec extends IntegrationSpec {

  trait Test {
    val nino          = "AA123456B"
    val correlationId = "X-123"

    val acceptHeader: String = "application/vnd.hmrc.2.0+json"

    def uri: String           = s"/ni/$nino/charitable-giving/2018-19"
    def downstreamUri: String = s"/2.0/ni/$nino/charitable-giving/2018-19"

    val jsonRequest: JsObject  = Json.obj("test" -> "json request")
    val jsonResponse: JsObject = Json.obj("test" -> "json response")

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, acceptHeader))
    }
  }

  "GET Charitable Giving annuals with release-2 enabled" should {
    s"return status 200 with a json response body" when {
      "the downstream response from the Charitable Giving api version 2 returns status 200 with a json response body" in new Test {
        override val jsonResponse: JsObject = Json
          .parse(
            s"""{
             |  "giftAidPayments": {
             |    "specifiedYear": 10000.00,
             |    "oneOffSpecifiedYear": 1000.00,
             |    "specifiedYearTreatedAsPreviousYear": 300.00,
             |    "followingYearTreatedAsSpecifiedYear": 400.00,
             |    "nonUKCharities": 2000.00,
             |    "nonUKCharityNames": ["International Charity A","International Charity B"]
             |  },
             |  "gifts": {
             |    "landAndBuildings": 700.00,
             |    "sharesOrSecurities": 600.00,
             |    "investmentsNonUKCharities": 300.00,
             |    "investmentsNonUKCharityNames": ["International Charity C","International Charity D"]
             |  }
             |}""".stripMargin
          )
          .as[JsObject]

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          //            MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET,
                                   downstreamUri,
                                   OK,
                                   jsonResponse,
                                   requestHeaders = Map(ACCEPT             -> acceptHeader),
                                   responseHeaders = Map("X-CorrelationId" -> correlationId))
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.header("X-CorrelationId") shouldBe Some(correlationId)
        response.json shouldBe jsonResponse
      }
    }
  }

  "Amend Charitable Giving with Version 2 enabled" should {

    "return a 204 with no json response body" when {
      "a version 2.0 header is provided and the response from the Charitable Giving API is a 204" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          //            MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, jsonResponse, requestHeaders = Map(ACCEPT -> acceptHeader))
        }

        val response: WSResponse = await(request.put(jsonRequest))
        response.status shouldBe NO_CONTENT
      }
    }
  }
}
