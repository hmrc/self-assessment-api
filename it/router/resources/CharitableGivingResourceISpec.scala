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

import play.api.libs.json.{JsObject, JsValue, Json}
import support.IntegrationSpec
import uk.gov.hmrc.http.HttpResponse

class CharitableGivingResourceISpec extends IntegrationSpec {

  val jsonRequest: JsObject = Json.obj("test" -> "json request")
  val jsonResponse: JsObject = Json.obj("test" -> "json response")

  val body: JsValue = Json.parse(
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

  val correlationId = "X-123"
  val testHeader = Map("X-CorrelationId" -> Seq(correlationId), "X-Content-Type-Options" -> Seq("nosniff"))

  val httpResponse = HttpResponse(OK, Some(body), testHeader)

 "GET Charitable Giving annuals with release-2 enabled" should {

      s"return status 200 with a json response body" when {
        "the downstream response from the Charitable Giving api version 2 returns status 200 with a json response body" in {
          val incomingUrl = s"/ni/AA111111A/charitable-giving/2018-19"
          val outgoingUrl = s"/2.0/ni/AA111111A/charitable-giving/2018-19"

          Given()
            .theClientIsAuthorised
            .And()
            .get(outgoingUrl)
            .returns(aResponse
              .withStatus(OK)
              .withBody(body)
              .withHeader("X-CorrelationId", correlationId))
            .When()
            .get(incomingUrl)
            .withHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json")
            .Then()
            .statusIs(OK)
            .bodyIs(body)
            .containsHeaders(("X-CorrelationId", correlationId))
            .verify(mockFor(outgoingUrl)
              .receivedHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json"))
        }
      }
  }

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
