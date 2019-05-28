package router.resources

import play.api.libs.json.{JsObject, Json}
import support.IntegrationSpec

class CrystallisationResourceISpec extends IntegrationSpec {


  val taxYear = "2017-18"
  val nino = "AA111111A"

  val jsonRequest: JsObject = Json.obj("test" -> "json request")

  "create crystallisation" should {

    "return a response with no Json response" when {
      "a version 2.0 header is provided and the response from the crystallisation API is 204" in {

        val incomingUrl = s"/ni/$nino/$taxYear/crystallisation"
        val outgoingUrl = s"/2.0/ni/$nino/$taxYear/crystallisation"

        Given()
          .theClientIsAuthorised
          .And()
          .post(outgoingUrl)
          .returns(aResponse
            .withStatus(NO_CONTENT)
            .withBody(""))
          .When()
          .post(incomingUrl)
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

  "Intent to crystallise" should {
    val location = "/self-assessment/ni/AA111111A/calculations/someCalculationId"

    "return a 303 with no json response body" when {

      "a version 2.0 header is provided and the response from the API is a 204" in {
        // Note V2 requires empty body
        val emptyBody = ""
        val incomingUrl = s"/ni/$nino/$taxYear/intent-to-crystallise"
        val outgoingUrl = s"/2.0/ni/$nino/$taxYear/intent-to-crystallise"

        Given()
          .theClientIsAuthorised
          .And()
          .post(outgoingUrl)
          .returns(aResponse
            .withStatus(SEE_OTHER)
            .withBody("")
            .withHeader(
              LOCATION, location))
          .When()
          .post(incomingUrl)
          .withBody(emptyBody)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.2.0+json"
          )
          .Then()
          .statusIs(SEE_OTHER)
          .bodyIs("")
          .containsHeaders(
            LOCATION -> location)
          .verify(mockFor(outgoingUrl)
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json"))
      }
    }
  }

  "Retrieve obligations" should {

    val body: String =
      """
        |{
        |  "obligations": [
        |    {
        |    	"identification": {
        |				"incomeSourceType": "ITSA",
        |				"referenceNumber": "AB123456A",
        |				"referenceType": "NINO"
        |			},
        |    "obligationDetails": [
        |      {
        |        "status": "O",
        |        "inboundCorrespondenceFromDate": "2018-02-01",
        |        "inboundCorrespondenceToDate": "2018-02-28",
        |        "inboundCorrespondenceDateReceived": "2018-04-01",
        |        "inboundCorrespondenceDueDate": "2018-05-28"
        |      }
        |    ]
        |    }
        |  ]
        |}
      """.stripMargin

    "return a status 200 and a body containing an obligation" when {
      "a version 2 header is provided in the request" in {
        val incomingUrl = s"/ni/$nino/crystallisation/obligations"
        val outgoingUrl = s"/2.0/ni/$nino/crystallisation/obligations"
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
