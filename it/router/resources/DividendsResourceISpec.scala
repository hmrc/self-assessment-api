package router.resources

import play.api.libs.json.{JsObject, Json}
import support.IntegrationSpec

class DividendsResourceISpec extends IntegrationSpec{

  val jsonRequest: JsObject = Json.obj("test" -> "json request")
  val jsonResponse: JsObject = Json.obj("test" -> "json response")

  val body: String =
    s"""{
       |  "ukDividends": 1000.00,
       |  "otherUkDividends": 2000.00
       |}""".stripMargin

  val correlationId = "X-123"
  val testHeader = Map("X-CorrelationId" -> Seq(correlationId), "X-Content-Type-Options" -> Seq("nosniff"))

  "Amend Dividends income with Version 2 enabled" should {

    "return a 204 with no json response body" when {
      "a version 1.0 header is provided and the response from the Dividends income API is a 204" in {
        val incomingUrl = "/ni/AA111111A/dividends/2018-19"
        val outgoingUrl = "/ni/AA111111A/dividends/2018-19"

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

      "a version 2.0 header is provided and the response from the Dividends income API is a 204" in {
        val incomingUrl = "/ni/AA111111A/dividends/2018-19"
        val outgoingUrl = "/2.0/ni/AA111111A/dividends/2018-19"

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

  "Retrieve" should {
    "return response with status 200 and body contains dividends income" when {
      "version 2.0 header is provided in the request" in {
        val incomingUrl = "/ni/AA111111A/dividends/2018-19"
        val outgoingUrl = "/2.0/ni/AA111111A/dividends/2018-19"

        Given()
          .theClientIsAuthorised
          .And()
          .get(outgoingUrl)
          .returns(aResponse.withBody(Json.parse(body)))
          .When()
          .get(incomingUrl)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.2.0+json",
            CONTENT_TYPE -> JSON
          )
          .Then()
          .statusIs(OK)
          .bodyIs(Json.parse(body))
          .verify(mockFor(outgoingUrl)
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json"))

      }
    }
  }
}
