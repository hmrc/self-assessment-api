package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.selfassessmentapi.resources.GovTestScenarioHeader
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode
import uk.gov.hmrc.support.BaseFunctionalSpec

class ClientSubscriptionSimulationSpec extends BaseFunctionalSpec {

  "Request for self-employments with Gov-Test-Scenario = CLIENT_NOT_SUBSCRIBED" should {
    "return HTTP 403 with error code informing client should be subscribed to MTD" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/self-employments")
        .withHeaders(GovTestScenarioHeader, "CLIENT_NOT_SUBSCRIBED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.CLIENT_NOT_SUBSCRIBED.toString)
    }
  }

  "Request for dividends with Gov-Test-Scenario = CLIENT_NOT_SUBSCRIBED" should {
    "return HTTP 403 with error code informing client should be subscribed to MTD" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(s"/ni/$nino/dividends/$taxYear")
        .withHeaders(GovTestScenarioHeader, "CLIENT_NOT_SUBSCRIBED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.CLIENT_NOT_SUBSCRIBED.toString)
    }
  }
}
