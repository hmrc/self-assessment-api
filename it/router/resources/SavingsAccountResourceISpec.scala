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

class SavingsAccountResourceISpec extends IntegrationSpec {

  "create savings account with Version 2 enabled" should {

    trait Test {
      val nino = "AA123456B"
      val id   = "SAVKB2UVwUTBQGJ"

      val jsonRequest: JsObject  = Json.obj("accountName" -> "Main account name")
      val jsonResponse: JsObject = Json.obj("id"          -> id)

      val correlationId = "X-123"

      val acceptHeader: String = "application/vnd.hmrc.2.0+json"

      def uri: String           = s"/ni/$nino/savings-accounts"
      def downstreamUri: String = s"/2.0/ni/$nino/savings-accounts"

      def setupStubs(): StubMapping

      def request: WSRequest = {
        setupStubs()
        buildRequest(uri)
          .withHttpHeaders((ACCEPT, acceptHeader))
      }
    }

    "return a 201 with no json response body" when {
      "a version 2.0 header is provided and the response from the savings accounts API is a 201" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.POST,
                                   downstreamUri,
                                   CREATED,
                                   jsonResponse,
                                   requestHeaders = Map(ACCEPT             -> acceptHeader),
                                   responseHeaders = Map("X-CorrelationId" -> correlationId))
        }

        val response: WSResponse = await(request.post(jsonRequest))
        response.status shouldBe CREATED
        response.header("X-CorrelationId") shouldBe Some(correlationId)
        response.json shouldBe jsonResponse
      }
    }
  }

  "retrieveAll" should {

    trait Test {
      val nino = "AA123456B"

      val jsonRequest: JsObject = Json.obj("accountName" -> "Main account name")
      val jsonResponse: JsObject = Json
        .parse(
          s"""{
             |    "savingsAccounts": [
             |        {
             |            "id": "SAVKB2UVwUTBQGJ",
             |            "accountName": "Main account name"
             |        },
             |        {
             |            "id": "SAVKB2UVwUTBQGK",
             |            "accountName": "Shares savings account"
             |        }
             |    ]
             |}""".stripMargin
        )
        .as[JsObject]

      val acceptHeader: String = "application/vnd.hmrc.2.0+json"

      def uri: String           = s"/ni/$nino/savings-accounts"
      def downstreamUri: String = s"/2.0/ni/$nino/savings-accounts"

      def setupStubs(): StubMapping

      def request: WSRequest = {
        setupStubs()
        buildRequest(uri)
          .withHttpHeaders((ACCEPT, acceptHeader))
      }
    }

    "return response with status 200 and body contains savings accounts" when {
      "version 2.0 header is provided in the request" in new Test {
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

  "retrieve" should {

    trait Test {
      val nino = "AA123456B"
      val id   = "SAVKB2UVwUTBQGJ"

      val jsonResponse: JsObject = Json.obj("accountName" -> "Main account name")

      val acceptHeader: String = "application/vnd.hmrc.2.0+json"

      def uri: String           = s"/ni/$nino/savings-accounts/$id"
      def downstreamUri: String = s"/2.0/ni/$nino/savings-accounts/$id"

      def setupStubs(): StubMapping

      def request: WSRequest = {
        setupStubs()
        buildRequest(uri)
          .withHttpHeaders((ACCEPT, acceptHeader))
      }
    }

    "return response with status 200 and body contains a savings account" when {
      "version 2.0 header is provided in the request" in new Test {
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

  "amend" should {

    trait Test {
      val nino    = "AA123456B"
      val id      = "SAVKB2UVwUTBQGJ"
      val taxYear = "2017-18"

      val jsonRequest: JsObject  = Json.obj("accountName" -> "Main account name")
      val jsonResponse: JsObject = Json.obj()

      val acceptHeader: String = "application/vnd.hmrc.2.0+json"

      def uri: String           = s"/ni/$nino/savings-accounts/$id/$taxYear"
      def downstreamUri: String = s"/2.0/ni/$nino/savings-accounts/$id/$taxYear"

      def setupStubs(): StubMapping

      def request: WSRequest = {
        setupStubs()
        buildRequest(uri)
          .withHttpHeaders((ACCEPT, acceptHeader))
      }
    }

    "return a 204 with no json response" when {
      "a version 2.0 header is provided and the response from the savings accounts API is a 204" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, jsonResponse, requestHeaders = Map(ACCEPT -> acceptHeader))
        }

        val response: WSResponse = await(request.put(jsonRequest))
        response.status shouldBe NO_CONTENT
      }
    }
  }

  "retrieveAnnual" should {

    trait Test {
      val nino    = "AA123456B"
      val id      = "SAVKB2UVwUTBQGJ"
      val taxYear = "2017-18"

      val jsonResponse: JsObject = Json.obj("accountName" -> "Main account name")

      val acceptHeader: String = "application/vnd.hmrc.2.0+json"

      def uri: String           = s"/ni/$nino/savings-accounts/$id/$taxYear"
      def downstreamUri: String = s"/2.0/ni/$nino/savings-accounts/$id/$taxYear"

      def setupStubs(): StubMapping

      def request: WSRequest = {
        setupStubs()
        buildRequest(uri)
          .withHttpHeaders((ACCEPT, acceptHeader))
      }
    }

    "return response with status 200 and body contains a savings account" when {
      "version 2.0 header is provided in the request" in new Test {
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
