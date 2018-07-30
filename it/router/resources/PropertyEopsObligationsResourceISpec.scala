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

class PropertyEopsObligationsResourceISpec extends IntegrationSpec {

  val jsonRequest: JsObject = Json.obj("test" -> "json request")
  val jsonResponse: JsObject = Json.obj("test" -> "json response")

  "GET Property End of Period Statements" should {
    "return a 200 with a json response body" when {
      "the downstream response from UK Property version 2.0 returns a 200 with a json response body" in {
        val incomingUrl = "/ni/AA111111A/uk-properties/end-of-period-statements/obligations?from=2017-04-06&to=2018-04-05"
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
