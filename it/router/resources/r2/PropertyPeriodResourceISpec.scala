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
import play.api.libs.json.{ JsObject, Json }
import play.api.libs.ws.{ WSRequest, WSResponse }
import support.ReleaseTwoIntegrationSpec
import support.stubs.{ AuthStub, DownstreamStub }

class PropertyPeriodResourceISpec extends ReleaseTwoIntegrationSpec {

  val propertyTypes = Seq("other", "furnished-holiday-lettings")

  "Create Property periods with release-2 enabled" should {

    trait Test {
      val nino = "AA123456B"

      val acceptHeader: String
      val propertyType: String

      def uri: String           = s"/ni/$nino/uk-properties/$propertyType/periods"
      def downstreamUri: String = s"/r2/ni/$nino/uk-properties/$propertyType/periods"

      val jsonRequest: JsObject = Json.obj("test" -> "json request")
      val jsonResponse: JsObject = Json.obj("test" -> "json response")

      def setupStubs(): StubMapping

      def request: WSRequest = {
        setupStubs()
        buildRequest(uri)
          .withHttpHeaders((ACCEPT, acceptHeader))
      }
    }

    for (p <- propertyTypes) {

      s"return a 201 with no json response body for $p properties" when {
        "the downstream response from the self assessment api version 1.0 returns a 201 with a json response body" in new Test {
          override val propertyType: String = p
          override val acceptHeader: String = "application/vnd.hmrc.1.0+json"

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            //            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, CREATED, jsonResponse)
          }

          val response: WSResponse = await(request.post(jsonRequest))
          response.status shouldBe CREATED
        }

        "the downstream response from the self assessment api version 2.0 returns a 201 with a json response body" in new Test {
          override val propertyType: String = p
          override val acceptHeader: String = "application/vnd.hmrc.2.0+json"

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            //            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, CREATED, jsonResponse)
          }

          val response: WSResponse = await(request.post(jsonRequest))
          response.status shouldBe CREATED
        }
      }
    }
  }

  "GET Property periods with release-2 enabled" should {

    trait Test {
      val nino = "AA123456B"

      val acceptHeader: String
      val propertyType: String

      def uri: String           = s"/ni/$nino/uk-properties/$propertyType/periods/periodId"
      def downstreamUri: String = s"/r2/ni/$nino/uk-properties/$propertyType/periods/periodId"

      val jsonResponse: JsObject = Json.obj("test" -> "json response")

      def setupStubs(): StubMapping

      def request: WSRequest = {
        setupStubs()
        buildRequest(uri)
          .withHttpHeaders((ACCEPT, acceptHeader))
      }
    }

    for (p <- propertyTypes) {

      s"return a 200 with a json response body for $p properties" when {
        "the downstream response from the self assessment api returns returns a 200 with a json response body" in new Test {
          override val propertyType: String = p
          override val acceptHeader: String = "application/vnd.hmrc.1.0+json"

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            //            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, jsonResponse, requestHeaders = Map(ACCEPT -> acceptHeader))
          }

          val response: WSResponse = await(request.get)
          response.status shouldBe OK
          response.json shouldBe jsonResponse
        }

        "a version 2.0 header is provided and the downstream response from the self assessment api returns a 200 with a json response body" in new Test {
          override val propertyType: String = p
          override val acceptHeader: String = "application/vnd.hmrc.2.0+json"

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            //            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, jsonResponse, requestHeaders = Map(ACCEPT -> "application/vnd.hmrc.1.0+json"))
          }

          val response: WSResponse = await(request.get)
          response.status shouldBe OK
          response.json shouldBe jsonResponse
        }
      }
    }
  }

  "PUT Property periods with release-2 enabled" should {

    trait Test {
      val nino = "AA123456B"

      val acceptHeader: String
      val propertyType: String

      def uri: String           = s"/ni/$nino/uk-properties/$propertyType/periods/periodId"
      def downstreamUri: String = s"/r2/ni/$nino/uk-properties/$propertyType/periods/periodId"

      val jsonRequest: JsObject = Json.obj("test" -> "json request")
      val jsonResponse: JsObject = Json.obj("test" -> "json response")

      def setupStubs(): StubMapping

      def request: WSRequest = {
        setupStubs()
        buildRequest(uri)
          .withHttpHeaders((ACCEPT, acceptHeader))
      }
    }

    for (p <- propertyTypes) {

      s"return a 204 with no json response body for $p properties" when {
        "the downstream response from the self assessment api returns a 204 with a json response body" in new Test {
          override val propertyType: String = p
          override val acceptHeader: String = "application/vnd.hmrc.1.0+json"

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            //            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, jsonResponse, requestHeaders = Map(ACCEPT -> acceptHeader))
          }

          val response: WSResponse = await(request.put(jsonRequest))
          response.status shouldBe NO_CONTENT
        }
        "a version 2.0 header is provided and the downstream response from the self assessment api returns a 204 with a json response body" in new Test {
          override val propertyType: String = p
          override val acceptHeader: String = "application/vnd.hmrc.2.0+json"

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            //            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, jsonResponse, requestHeaders = Map(ACCEPT -> "application/vnd.hmrc.1.0+json"))
          }

          val response: WSResponse = await(request.put(jsonRequest))
          response.status shouldBe NO_CONTENT
        }
      }
    }
  }
}
