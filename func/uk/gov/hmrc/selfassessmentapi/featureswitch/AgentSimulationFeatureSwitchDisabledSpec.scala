package uk.gov.hmrc.selfassessmentapi.featureswitch

import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.resources.GovTestScenarioHeader
import uk.gov.hmrc.support.BaseFunctionalSpec

class AgentSimulationFeatureSwitchDisabledSpec extends BaseFunctionalSpec {

  private val conf = Map("Test.feature-switch.test-scenario-simulation.enabled" -> false)

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = conf)

  "Agent simulation filters" should {
    "not be applied if feature is switched off" in {
      given()
        .userIsFullyAuthorisedForTheResource(nino)
        .des().selfEmployment.willBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_SUBSCRIBED")
        .thenAssertThat()
        .statusIs(200)
    }
  }

}


