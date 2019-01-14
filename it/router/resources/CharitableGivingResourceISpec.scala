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
import router.constants.Versions.VERSION_2

class CharitableGivingResourceISpec extends IntegrationSpec {

  val jsonRequest: JsObject = Json.obj("test" -> "json request")
  val jsonResponse: JsObject = Json.obj("test" -> "json response")

// NOT YET REQUIRED!!   At this time, only amend (PUT) is being addressed. But I leave this here for whom ever picks up retrieve
/* "GET Charitable Giving annuals with release-2 enabled" should {

      s"return a 200 with a json response body" when {
        "the downstream response from the Charitable Giving api release 2 returns returns a 200 with a json response body" in {
          val incomingUrl = s"/ni/AA111111A/charitable-giving/2018-19"
          val outgoingUrl = s"/r2/ni/AA111111A/charitable-giving/2018-19"
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

        "a version 2.0 header is provided and the downstream response from the Charitable Giving api returns a 200 with a json response body" in {
          val incomingUrl = s"/ni/AA111111A/charitable-giving/2018-19"
          val outgoingUrl = s"/r2/ni/AA111111A/charitable-giving/2018-19"

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
  }*/

  "Amend Charitable Giving with Version 2 enabled" should {

      "return a 204 with no json response body" when {
        "a version 1.0 header is provided and the response from the Charitable Giving API is a 204" in {
          val incomingUrl = s"/ni/AA111111A/charitable-giving/2018-19"
          val outgoingUrl = s"/ni/AA111111A/charitable-giving/2018-19"

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

        "a version 2.0 header is provided and the response from the Charitable Giving API is a 204" in {
          val incomingUrl = s"/ni/AA111111A/charitable-giving/2018-19"
          val outgoingUrl = s"/2.0/ni/AA111111A/charitable-giving/2018-19"

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
              .receivedHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json"))
        }
      }
  }
}
