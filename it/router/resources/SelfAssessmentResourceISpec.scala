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

package router.resources

import play.api.libs.json.{JsObject, Json}
import support.IntegrationSpec

class SelfAssessmentResourceISpec extends IntegrationSpec {

  val jsonRequest: JsObject = Json.obj("test" -> "json request")
  val jsonResponse: JsObject = Json.obj("test" -> "json response")

  val locationValue = "test-location-value"

  "GET /any/url" should {
    "return a 200 with a json response body" when {
      "the downstream response from the self assessment api returns a 200 with a json response body" in {
        Given()
          .theClientIsAuthorised
        .And()
          .get("/any/url")
            .returns(aResponse
              .withStatus(OK)
              .withBody(jsonResponse))
        .When()
          .get("/any/url")
          .withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json")
        .Then()
          .statusIs(OK)
          .bodyIs(jsonResponse)
          .verify(mockFor("/any/url")
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json"))
      }

      "a version 2.0 header is provided and the downstream response from the self assessment api returns a 200 with a json response body" in {
        Given()
          .theClientIsAuthorised
        .And()
          .get("/any/url")
            .returns(aResponse
              .withStatus(OK)
              .withBody(jsonResponse))
        .When()
          .get("/any/url")
          .withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json")
        .Then()
          .statusIs(OK)
          .bodyIs(jsonResponse)
          .verify(mockFor("/any/url")
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json"))
      }
    }
  }

  "POST /any/url with a json request body" should {
    "return a 200 with a json response body" when {
      "the downstream response from the self assessment api returns a 200 with a json response body and location header" in {
        Given()
          .theClientIsAuthorised
        .And()
          .post("/any/url")
            .returns(aResponse
              .withStatus(OK)
              .withBody(jsonResponse)
              .withHeader(LOCATION, locationValue))
        .When()
          .post("/any/url")
          .withBody(jsonRequest)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.1.0+json",
            CONTENT_TYPE -> JSON
          )
        .Then()
          .statusIs(OK)
          .bodyIs(jsonResponse)
          .containsHeaders(LOCATION -> locationValue)
          .verify(mockFor("/any/url")
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json"))
      }
    }
  }

  "PUT /any/url with a json request body" should {
    "return a 200 with a json response body" when {
      "the downstream response from the self assessment api returns a 200 with a json response body" in {
        Given()
          .theClientIsAuthorised
        .And()
          .put("/any/url")
            .returns(aResponse
              .withStatus(OK)
              .withBody(jsonResponse))
        .When()
          .put("/any/url")
          .withBody(jsonRequest)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.1.0+json",
            CONTENT_TYPE -> JSON
          )
        .Then()
          .statusIs(OK)
          .bodyIs(jsonResponse)
          .verify(mockFor("/any/url")
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json"))
      }
    }
  }
}
