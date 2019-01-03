/*
 * Copyright 2018 HM Revenue & Customs
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

class SelfEmploymentAnnualsResourceISpec extends IntegrationSpec {

  val jsonRequest: JsObject = Json.obj("test" -> "json request")
  val jsonResponse: JsObject = Json.obj("test" -> "json response")

  val selfEmploymentId = "test-selfemployment-id"

  "GET Self Employment annuals with release-2 disabled" should {

      s"return a 200 with a json response body" when {
        "the downstream response from the self assessment api returns returns a 200 with a json response body" in {
          val incomingUrl = s"/ni/AA111111A/self-employments/$selfEmploymentId/2018-19"
          val outgoingUrl = s"/ni/AA111111A/self-employments/$selfEmploymentId/2018-19"
          Given()
            .theClientIsAuthorised
            .And()
            .get(outgoingUrl)
            .returns(aResponse
              .withStatus(OK)
              .withBody(jsonResponse))
            .When()
            .get(incomingUrl)
            .withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json")
            .Then()
            .statusIs(OK)
            .bodyIs(jsonResponse)
            .verify(mockFor(outgoingUrl)
              .receivedHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json"))
        }

        "a version 2.0 header is provided and the downstream response from the self assessment api returns a 200 with a json response body" in {
          val incomingUrl = s"/ni/AA111111A/self-employments/$selfEmploymentId/2018-19"
          val outgoingUrl = s"/ni/AA111111A/self-employments/$selfEmploymentId/2018-19"

          Given()
            .theClientIsAuthorised
            .And()
            .get(outgoingUrl)
            .returns(aResponse
              .withStatus(OK)
              .withBody(jsonResponse))
            .When()
            .get(incomingUrl)
            .withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json")
            .Then()
            .statusIs(OK)
            .bodyIs(jsonResponse)
            .verify(mockFor(outgoingUrl)
              .receivedHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json"))
        }
      }
  }

  "PUT Self Employment annuals with release-2 disabled" should {

      s"return a 204 with no json response body for $selfEmploymentId properties" when {
        "a version 1.0 header is provided and the downstream response from the self assessment api returns a 204 with a json response body" in {
          val incomingUrl = s"/ni/AA111111A/self-employments/$selfEmploymentId/2018-19"
          val outgoingUrl = s"/ni/AA111111A/self-employments/$selfEmploymentId/2018-19"

          Given()
            .theClientIsAuthorised
            .And()
            .put(outgoingUrl)
            .returns(aResponse
              .withStatus(NO_CONTENT)
              .withBody(jsonResponse))
            .When()
            .put(incomingUrl)
            .withBody(jsonRequest)
            .withHeaders(
              ACCEPT -> "application/vnd.hmrc.1.0+json",
              CONTENT_TYPE -> JSON
            )
            .Then()
            .statusIs(NO_CONTENT)
            .bodyIs("")
            .verify(mockFor(outgoingUrl)
              .receivedHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json"))
        }

        "a version 2.0 header is provided and the downstream response from the self assessment api returns a 204 with a json response body" in {
          val incomingUrl = s"/ni/AA111111A/self-employments/$selfEmploymentId/2018-19"
          val outgoingUrl = s"/ni/AA111111A/self-employments/$selfEmploymentId/2018-19"

          Given()
            .theClientIsAuthorised
            .And()
            .put(outgoingUrl)
            .returns(aResponse
              .withStatus(NO_CONTENT)
              .withBody(jsonResponse))
            .When()
            .put(incomingUrl)
            .withBody(jsonRequest)
            .withHeaders(
              ACCEPT -> "application/vnd.hmrc.2.0+json",
              CONTENT_TYPE -> JSON
            )
            .Then()
            .statusIs(NO_CONTENT)
            .bodyIs("")
            .verify(mockFor(outgoingUrl)
              .receivedHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json"))
        }
      }
  }
}
