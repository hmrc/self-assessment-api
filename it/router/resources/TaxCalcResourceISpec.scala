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

class TaxCalcResourceISpec extends IntegrationSpec {

  val jsonRequest: JsObject = Json.obj("test" -> "json request")
  val jsonResponse: JsObject = Json.obj("test" -> "json response")

  "GET Tax Calculation" should {
    "return a 200 with a json response body" when {
      "the downstream response from the self assessment api version 1.0 returns a 200 with a json response body" in {
        val url = "/ni/AA111111A/calculations/041f7e4d-87d9-4d4a-a296-3cfbdf92f7e2"

        Given()
          .theClientIsAuthorised
        .And()
          .get(url)
            .returns(aResponse
              .withStatus(OK)
              .withBody(jsonResponse))
        .When()
          .get(url)
          .withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json")
        .Then()
          .statusIs(OK)
          .bodyIs(jsonResponse)
          .verify(mockFor(url)
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json"))
      }
      "the downstream response from the self assessment api version 2.0 returns a 200 with a json response body" in {
        val incomingUrl = "/ni/AA111111A/calculations/041f7e4d-87d9-4d4a-a296-3cfbdf92f7e2"
        val outgoingUrl = "/2.0" + incomingUrl

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
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json"))
      }
    }
  }

  "GET Tax Calculation Messages" should {
    "return a 404" when {
      "the downstream response from the self assessment api version 1.0 returns a 404" in {
        val url = "/ni/AA111111A/calculations/041f7e4d-87d9-4d4a-a296-3cfbdf92f7e2/validation-messages"

        Given()
          .theClientIsAuthorised
          .And()
          .get(url)
          .returns(aResponse
            .withStatus(NOT_FOUND))
          .When()
          .get(url)
          .withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json")
          .Then()
          .statusIs(NOT_FOUND)
          .verify(mockFor(url)
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json"))
      }
    }
    "return a 200 with json response body" when {
      "the downstream response from the self assessment api version 2.0 returns a 200 with a json response body" in {
        val incomingUrl = "/ni/AA111111A/calculations/041f7e4d-87d9-4d4a-a296-3cfbdf92f7e2/validation-messages"
        val outgoingUrl = "/2.0" + incomingUrl

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
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json"))
      }
    }
  }
}
