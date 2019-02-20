package router.resources

import play.api.libs.json.{JsObject, Json}
import support.IntegrationSpec

class SavingsAccountResourceISpec extends IntegrationSpec{

  val jsonRequest: JsObject = Json.obj("accountName" -> "Main account name")
  val jsonResponse: JsObject = Json.obj("id" -> "SAVKB2UVwUTBQGJ")

  val correlationId = "X-123"

  "create savings account with Version 2 enabled" should {

    "return a 201 with no json response body" when {
      "a version 1.0 header is provided and the response from the savings accounts API is a 201" in {
        val incomingUrl = "/ni/AA111111A/savings-accounts"
        val outgoingUrl = "/ni/AA111111A/savings-accounts"

        Given()
          .theClientIsAuthorised
          .And()
          .post(outgoingUrl)
          .returns(aResponse
            .withStatus(CREATED)
            .withBody(jsonResponse))
          .When()
          .post(incomingUrl)
          .withBody(jsonRequest)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.1.0+json",
            CONTENT_TYPE -> JSON
          )
          .Then()
          .statusIs(CREATED)
          .bodyIs(jsonResponse)
          .verify(mockFor(outgoingUrl)
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json"))
      }

      "a version 2.0 header is provided and the response from the savings accounts API is a 201" in {
        val incomingUrl = "/ni/AA111111A/savings-accounts"
        val outgoingUrl = "/2.0/ni/AA111111A/savings-accounts"

        Given()
          .theClientIsAuthorised
          .And()
          .post(outgoingUrl)
          .returns(aResponse
            .withStatus(CREATED)
            .withBody(jsonResponse)
            .withHeader("X-CorrelationId", correlationId))
          .When()
          .post(incomingUrl)
          .withBody(jsonRequest)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.2.0+json",
            CONTENT_TYPE -> JSON
          )
          .Then()
          .statusIs(CREATED)
          .bodyIs(jsonResponse)
          .containsHeaders(("X-CorrelationId", correlationId))
          .verify(mockFor(outgoingUrl)
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json"))
      }
    }
  }
}
