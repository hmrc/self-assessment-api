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
import play.api.libs.json.{ JsNull, JsObject, Json }
import play.api.libs.ws.{ WSRequest, WSResponse }
import support.IntegrationSpec
import support.stubs.{ AuthStub, DownstreamStub }

class CrystallisationResourceISpec extends IntegrationSpec {

  "create crystallisation" should {

    trait Test {
      val nino    = "AA123456B"
      val taxYear = "2017-18"

      val acceptHeader: String = "application/vnd.hmrc.2.0+json"

      def uri: String           = s"/ni/$nino/$taxYear/crystallisation"
      def downstreamUri: String = s"/2.0/ni/$nino/$taxYear/crystallisation"

      val jsonRequest: JsObject = Json.obj("test" -> "json request")

      def setupStubs(): StubMapping

      def request: WSRequest = {
        setupStubs()
        buildRequest(uri)
          .withHttpHeaders((ACCEPT, acceptHeader))
      }
    }

    "return a response with no Json response" when {
      "a version 2.0 header is provided and the response from the crystallisation API is 204" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, NO_CONTENT, Json.obj(), requestHeaders = Map(ACCEPT -> acceptHeader))
        }

        val response: WSResponse = await(request.post(jsonRequest))
        response.status shouldBe NO_CONTENT
      }
    }
  }

  "Intent to crystallise" should {

    trait Test {
      val nino    = "AA123456B"
      val taxYear = "2017-18"

      val location = "/self-assessment/ni/AA111111A/calculations/someCalculationId"

      val acceptHeader: String = "application/vnd.hmrc.2.0+json"

      def uri: String           = s"/ni/$nino/$taxYear/intent-to-crystallise"
      def downstreamUri: String = s"/2.0/ni/$nino/$taxYear/intent-to-crystallise"

      def setupStubs(): StubMapping

      def request: WSRequest = {
        setupStubs()
        buildRequest(uri)
          .withHttpHeaders((ACCEPT, acceptHeader))
      }
    }

    "return a 303 with no json response body" when {
      "a version 2.0 header is provided and the response from the API is a 204" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.POST,
                                   downstreamUri,
                                   SEE_OTHER,
                                   JsNull,
                                   requestHeaders = Map(ACCEPT    -> acceptHeader),
                                   responseHeaders = Map(LOCATION -> location))
        }

        val response: WSResponse = await(request.post(JsNull))
        response.status shouldBe SEE_OTHER
        response.header(LOCATION) shouldBe Some(location)
      }
    }
  }

  "Retrieve obligations" should {

    trait Test {
      val nino    = "AA123456B"
      val taxYear = "2017-18"

      val acceptHeader: String = "application/vnd.hmrc.2.0+json"

      def uri: String           = s"/ni/$nino/crystallisation/obligations"
      def downstreamUri: String = s"/2.0/ni/$nino/crystallisation/obligations"

      val jsonResponse: JsObject = Json.parse("""
        |{
        |  "obligations": [
        |    {
        |    	"identification": {
        |				"incomeSourceType": "ITSA",
        |				"referenceNumber": "AB123456A",
        |				"referenceType": "NINO"
        |			},
        |    "obligationDetails": [
        |      {
        |        "status": "O",
        |        "inboundCorrespondenceFromDate": "2018-02-01",
        |        "inboundCorrespondenceToDate": "2018-02-28",
        |        "inboundCorrespondenceDateReceived": "2018-04-01",
        |        "inboundCorrespondenceDueDate": "2018-05-28"
        |      }
        |    ]
        |    }
        |  ]
        |}
      """.stripMargin).as[JsObject]

      def setupStubs(): StubMapping

      def request: WSRequest = {
        setupStubs()
        buildRequest(uri)
          .withHttpHeaders((ACCEPT, acceptHeader))
      }
    }

    "return a status 200 and a body containing an obligation" when {
      "a version 2 header is provided in the request" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, jsonResponse, requestHeaders = Map(ACCEPT -> acceptHeader))
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.json shouldBe jsonResponse
      }
    }
  }

}
