package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.selfassessmentapi.models.ErrorCode
import uk.gov.hmrc.support.BaseFunctionalSpec
import uk.gov.hmrc.selfassessmentapi.resources.GovTestScenarioHeader

class AgentSubscriptionSimulationSpec extends BaseFunctionalSpec {

  "Request for self-employments with Gov-Test-Scenario = AGENT_NOT_SUBSCRIBED" should {
    "return HTTP 403 with error code informing Agent should be subscribed to Agent Services" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_SUBSCRIBED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_SUBSCRIBED.toString)
    }
  }

  "Request for dividends with Gov-Test-Scenario = AGENT_NOT_SUBSCRIBED" should {
    "return HTTP 403 with error code informing Agent should be subscribed to Agent Services" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .when()
        .post(s"/ni/$nino/dividends/$taxYear")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_SUBSCRIBED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_SUBSCRIBED.toString)
    }
  }
}
