package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.selfassessmentapi.resources.GovTestScenarioHeader
import uk.gov.hmrc.support.BaseFunctionalSpec

class TestScenarioHeaderSpec extends BaseFunctionalSpec {
  "Request for self-employments with no Gov-Test-Scenario" should {
    "return HTTP 200" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .des().selfEmployment.willBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
    }
  }

  "Request for self-employments with invalid Gov-Test-Scenario" should {
    "return HTTP 200" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .des().selfEmployment.willBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .withHeaders(GovTestScenarioHeader, "FOO")
        .thenAssertThat()
        .statusIs(200)
    }
  }

  "Request for dividends with no Gov-Test-Scenario" should {
    "return HTTP 200" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .des().selfEmployment.willBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/dividends/$taxYear")
        .thenAssertThat()
        .statusIs(200)
    }
  }

  "Request for dividends with invalid Gov-Test-Scenario" should {
    "return HTTP 200" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .des().selfEmployment.willBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/dividends/$taxYear")
        .withHeaders(GovTestScenarioHeader, "FOO")
        .thenAssertThat()
        .statusIs(200)
    }
  }
}
