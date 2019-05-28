package router.resources

import play.api.libs.json.{JsObject, JsValue, Json}
import support.IntegrationSpec

class SavingsAccountResourceISpec extends IntegrationSpec {

  val id = "SAVKB2UVwUTBQGJ"
  val taxYear = "2017-18"

  val jsonRequest: JsObject = Json.obj("accountName" -> "Main account name")
  val jsonResponse: JsObject = Json.obj("id" -> id)

  val correlationId = "X-123"

  val body: JsValue = Json.parse(
    s"""{
       |    "savingsAccounts": [
       |        {
       |            "id": "SAVKB2UVwUTBQGJ",
       |            "accountName": "Main account name"
       |        },
       |        {
       |            "id": "SAVKB2UVwUTBQGK",
       |            "accountName": "Shares savings account"
       |        }
       |    ]
       |}""".stripMargin
  )

  val singleBody: JsValue = Json.parse(
    s"""{
       |    "accountName": "Main account name"
       |}""".stripMargin
  )

  "create savings account with Version 2 enabled" should {

    "return a 201 with no json response body" when {
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

  "retrieveAll" should {
    "return response with status 200 and body contains savings accounts" when {
      "version 2.0 header is provided in the request" in {
        val incomingUrl = "/ni/AA111111A/savings-accounts"
        val outgoingUrl = "/2.0/ni/AA111111A/savings-accounts"

        Given()
          .theClientIsAuthorised
          .And()
          .get(outgoingUrl)
          .returns(aResponse.withBody(body))
          .When()
          .get(incomingUrl)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.2.0+json",
            CONTENT_TYPE -> JSON
          )
          .Then()
          .statusIs(OK)
          .bodyIs(body)
          .verify(mockFor(outgoingUrl)
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json"))

      }
    }
  }

  "retrieve" should {
    "return response with status 200 and body contains a savings account" when {
      "version 2.0 header is provided in the request" in {
        val incomingUrl = s"/ni/AA111111A/savings-accounts/$id"
        val outgoingUrl = s"/2.0/ni/AA111111A/savings-accounts/$id"

        Given()
          .theClientIsAuthorised
          .And()
          .get(outgoingUrl)
          .returns(aResponse.withBody(singleBody))
          .When()
          .get(incomingUrl)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.2.0+json",
            CONTENT_TYPE -> JSON
          )
          .Then()
          .statusIs(OK)
          .bodyIs(singleBody)
          .verify(mockFor(outgoingUrl)
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json"))

      }
    }
  }

  "amend" should {
    "return a 204 with no json response" when {
      "a version 2.0 header is provided and the response from the savings accounts API is a 204" in {
        val incomingUrl = s"/ni/AA111111A/savings-accounts/$id/$taxYear"
        val outgoingUrl = s"/2.0/ni/AA111111A/savings-accounts/$id/$taxYear"

        Given()
          .theClientIsAuthorised
          .And()
          .put(outgoingUrl)
          .returns(aResponse
            .withStatus(NO_CONTENT)
            .withBody(""))
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

  "retrieveAnnual" should {
    "return response with status 200 and body contains a savings account" when {
      "version 2.0 header is provided in the request" in {
        val incomingUrl = s"/ni/AA111111A/savings-accounts/$id/$taxYear"
        val outgoingUrl = s"/2.0/ni/AA111111A/savings-accounts/$id/$taxYear"

        Given()
          .theClientIsAuthorised
          .And()
          .get(outgoingUrl)
          .returns(aResponse.withBody(singleBody))
          .When()
          .get(incomingUrl)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.2.0+json",
            CONTENT_TYPE -> JSON
          )
          .Then()
          .statusIs(OK)
          .bodyIs(singleBody)
          .verify(mockFor(outgoingUrl)
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json"))

      }
    }
  }

}
