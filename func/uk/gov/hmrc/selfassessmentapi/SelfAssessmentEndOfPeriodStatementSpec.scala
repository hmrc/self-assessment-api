package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.support.BaseFunctionalSpec
import play.api.libs.json.Json

class SelfAssessmentEndOfPeriodStatementSpec extends BaseFunctionalSpec {

  "Submitting your self-assessment end-of-period statement" should {

    "succeed when all pre-requisites have been met" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.endOfYearStatementReadyToBeFinalised(nino)
        .when()
        .post(s"/ni/$nino/self-employments/abc/statements/${taxYear}", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(204)
    }

    "fail when the statement has not been declared final" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.endOfYearStatementReadyToBeFinalised(nino)
        .when()
        .post(s"/ni/$nino/self-employments/abc/statements/$taxYear", Some(Json.parse("""{ "finalised": false }""")))
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\errors(0)\\code", "NOT_FINALISED")
    }

    "fail when periodic update is missing from statement" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.endOfYearStatementMissingPeriod(nino)
        .when()
        .post(s"/ni/$nino/self-employments/abc/statements/$taxYear", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\errors(0)\\code", "PERIODIC_UPDATE_MISSING")
    }

    "fail when statement is finalised late" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.endOfYearStatementIsLate(nino)
        .when()
        .post(s"/ni/$nino/self-employments/abc/statements/$taxYear", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\errors(0)\\code", "LATE_SUBMISSION")
    }

    "fail when statement is already finalised" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.endOfYearStatementIsAlreadyFinalised(nino)
        .when()
        .post(s"/ni/$nino/self-employments/abc/statements/$taxYear", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\errors(0)\\code", "ALREADY_FINALISED")
    }

  }

}
