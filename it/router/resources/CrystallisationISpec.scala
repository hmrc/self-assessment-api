package router.resources

import play.api.libs.json.{JsObject, Json}
import support.IntegrationSpec

class CrystallisationISpec extends IntegrationSpec {


  val taxYear = "2017-18"
  val nino = "AA111111A"

  val jsonRequest: JsObject = Json.obj("test" -> "json request")
  val jsonResponse: JsObject = Json.obj("test" -> "json response")

  "create crystallisation" should {
    "return a response with no Json response" when {
      "a version 1.0 header is provided and the response from the crystallisation API is 201" in {

        val incomingUrl = s"/ni/$nino/$taxYear/crystallisation"
        val outgoingUrl = s"/ni/$nino/$taxYear/crystallisation"

        Given()
          .theClientIsAuthorised
          .And()
          .post(outgoingUrl)
          .returns(aResponse
            .withStatus(CREATED)
            .withBody(""))
          .When()
          .post(incomingUrl)
          .withBody(jsonRequest)
          .withHeaders(
            ACCEPT -> "application/vnd.hmrc.1.0+json",
            CONTENT_TYPE -> JSON
          )
          .Then()
          .statusIs(CREATED)
          .bodyIs("")
          .verify(mockFor(outgoingUrl)
            .receivedHeaders(ACCEPT -> "application/vnd.hmrc.1.0+json"))

      }

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
}
