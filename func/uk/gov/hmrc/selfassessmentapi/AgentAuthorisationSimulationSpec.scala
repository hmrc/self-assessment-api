package uk.gov.hmrc.selfassessmentapi

import play.api.libs.json.JsString
import uk.gov.hmrc.selfassessmentapi.resources.{GovTestScenarioHeader, Jsons}
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode
import uk.gov.hmrc.support.BaseFunctionalSpec

class AgentAuthorisationSimulationSpec extends BaseFunctionalSpec {

  "An unauthorized agent (i.e. a FOA) interacting with self-employments" should {
    // START GET
    "receive a HTTP 403 Unauthorized when they attempt to retrieve all self-employments" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/self-employments")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to retrieve a specific self-employment" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/self-employments/abc")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to retrieve a specific self-employment obligations" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/self-employments/abc/obligations")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to retrieve a specific self-employment annual summary" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/self-employments/abc/$taxYear")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to retrieve all specific self-employment periods" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to retrieve a specific self-employment period" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/self-employments/abc/periods/def")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    // END GET

    "receive a HTTP 403 Unauthorized when they attempt to create a self-employment using an invalid json" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(Jsons.SelfEmployment(accountingType = "NONSENSE")).to(s"/ni/$nino/self-employments")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to create a periodic summary using an invalid identifier" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(Jsons.SelfEmployment.period()).to(s"/ni/$nino/self-employments/invalid-id/periods")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to update a periodic summary with an invalid identifier" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.periodWillNotBeUpdatedFor(nino, from = "2017-04-05", to = "2018-04-04")
        .when()
        .put(Jsons.SelfEmployment.period()).at(s"/ni/$nino/self-employments/abc/periods/2017-04-05_2018-04-04")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to create a periodic summary with an invalid json" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.willBeCreatedFor(nino)
        .des().selfEmployment.willBeReturnedFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(Jsons.SelfEmployment.period(turnover = 100.1234)).to(s"%sourceLocation%/periods")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to update a periodic summary with an invalid json" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.willBeCreatedFor(nino)
        .des().selfEmployment.willBeReturnedFor(nino)
        .des().selfEmployment.periodWillBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2018-04-05")))
        .to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.SelfEmployment.period(turnover = 100.1234)).at(s"/ni/$nino/self-employments/abc/periods/2017-04-06_2018-04-05")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to update an annual summary with an invalid identifier" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.annualSummaryWillNotBeUpdatedFor(nino)
        .when()
        .put(Jsons.SelfEmployment.annualSummary()).at(s"/ni/$nino/self-employments/abc/$taxYear")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to create more than one self-employment source" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.tooManySourcesFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }
  }

  "An unauthorized agent (i.e. a FOA) interacting with properties" should {
    "receive a HTTP 403 Unauthorized when they attempt to retrieve property information" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/uk-properties")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to retrieve property obligations" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/uk-properties/obligations")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to retrieve property annual summaries" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/uk-properties/furnished-holiday-lettings/$taxYear")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
        .when()
        .get(s"/ni/$nino/uk-properties/other/$taxYear")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to retrieve all property periods" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/uk-properties/furnished-holiday-lettings/periods")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
        .when()
        .get(s"/ni/$nino/uk-properties/other/periods")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to retrieve specific 'other' property periods" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/uk-properties/other/periods")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to retrieve specific 'fhl' property periods" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/uk-properties/furnished-holiday-lettings/periods")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to create a property with an invalid json" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(JsString("NONSENSE")).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .isBadRequest
    }

    "receive a HTTP 403 Unauthorized when they attempt to update an annual summary with an invalid json" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .put(Jsons.Properties.otherAnnualSummary(annualInvestmentAllowance = 100.1234))
        .at(s"/ni/$nino/uk-properties/other/$taxYear")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to create a periodic summary with an invalid json" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(Jsons.Properties.otherPeriod(rentIncome = 1000.123))
        .to(s"/ni/$nino/uk-properties/other/periods")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when they attempt to update a periodic summary with an invalid identifier" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .put(Jsons.Properties.otherPeriod())
        .at(s"/ni/$nino/uk-properties/other/periods/invalid-id")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }

    "receive a HTTP 403 Unauthorized when attempting to create a UK property business for an unauthorised agent" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.notFoundIncomeSourceFor(nino)
        .when()
        .post(Jsons.Properties())
        .to(s"/ni/$nino/uk-properties")
        .withHeaders(GovTestScenarioHeader, "AGENT_NOT_AUTHORIZED")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsError(ErrorCode.AGENT_NOT_AUTHORIZED.toString)
    }
  }
}
