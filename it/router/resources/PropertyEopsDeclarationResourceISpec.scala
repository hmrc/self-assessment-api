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

class PropertyEopsDeclarationResourceISpec extends IntegrationSpec {

  val jsonRequest: JsObject = Json.obj("test" -> "json request")
  val jsonResponse: JsObject = Json.obj("test" -> "json response")

  "Post UK Property EOPS declarationo" should {
    "return a 204 with no json response body" when {
      "the downstream response from the self assessment api version 1.0 returns a 204 with no json response body" in {
        val url = "/ni/AA111111A/uk-properties/end-of-period-statements/from/2018-01-01/to/2018-12-12"

        Given()
          .theClientIsAuthorised
          .And()
          .post(url)
          .returns(aResponse
            .withStatus(NO_CONTENT))
          .When()
          .post(url)
            .withBody(jsonRequest)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.1.0+json",
            CONTENT_TYPE -> JSON)
          .Then()
          .statusIs(NO_CONTENT)
          .verify(mockFor(url)
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json"))
      }
      "the downstream response from the self assessment api version 2.0 returns a 204 with no json response body" in {
        val incomingUrl = "/ni/AA111111A/uk-properties/end-of-period-statements/from/2018-01-01/to/2018-12-12"
        val outgoingUrl = "/2.0" + incomingUrl

        Given()
          .theClientIsAuthorised
          .And()
          .post(outgoingUrl)
          .returns(aResponse
            .withStatus(NO_CONTENT))
          .When()
          .post(incomingUrl)
            .withBody(jsonRequest)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.2.0+json",
            CONTENT_TYPE -> JSON)
          .Then()
          .statusIs(NO_CONTENT)
          .verify(mockFor(outgoingUrl)
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json"))
      }
    }
  }
}
