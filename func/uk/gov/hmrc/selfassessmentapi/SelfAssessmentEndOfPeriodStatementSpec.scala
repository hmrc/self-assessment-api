package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.support.BaseFunctionalSpec
import play.api.libs.json.Json
import org.joda.time.LocalDate

class SelfAssessmentEndOfPeriodStatementSpec extends BaseFunctionalSpec {

  "Submitting your self-assessment end-of-period statement" should {

    val start = new LocalDate(2015, 1, 1)
    val end = new LocalDate(2016, 12, 31)

    "succeed when all pre-requisites have been met" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.endOfYearStatementReadyToBeFinalised(nino, start, end)
        .when()
        .post(s"/ni/$nino/self-employments/abc/end-of-period-statements/from/$start/to/$end", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(204)
    }

    "fail when the statement has not been declared final" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.endOfYearStatementReadyToBeFinalised(nino, start, end)
        .when()
        .post(s"/ni/$nino/self-employments/abc/end-of-period-statements/from/$start/to/$end", Some(Json.parse("""{ "finalised": false }""")))
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\errors(0)\\code", "NOT_FINALISED")
        .bodyHasPath("\\errors(0)\\path", "/finalised")
    }

    "fail when periodic update is missing from statement" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.endOfYearStatementMissingPeriod(nino, start, end)
        .when()
        .post(s"/ni/$nino/self-employments/abc/end-of-period-statements/from/$start/to/$end", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\code", "BUSINESS_ERROR")
        .bodyHasPath("\\errors(0)\\code", "PERIODIC_UPDATE_MISSING")
    }

    "fail when statement is already finalised" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.endOfYearStatementIsAlreadyFinalised(nino, start, end)
        .when()
        .post(s"/ni/$nino/self-employments/abc/end-of-period-statements/from/$start/to/$end", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\code", "BUSINESS_ERROR")
        .bodyHasPath("\\errors(0)\\code", "ALREADY_FINALISED")
    }

    "fail when invalid boolean value sent" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.endOfYearStatementReadyToBeFinalised(nino, start, end)
        .when()
        .post(s"/ni/$nino/self-employments/abc/end-of-period-statements/from/$start/to/$end", Some(Json.parse("""{ "finalised": null }""")))
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\errors(0)\\code", "INVALID_BOOLEAN_VALUE")
        .bodyHasPath("\\errors(0)\\path", "/finalised")
    }

    "fail when invalid from date value sent" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.endOfYearStatementReadyToBeFinalised(nino, start, end)
        .when()
        .post(s"/ni/$nino/self-employments/abc/end-of-period-statements/from/not-a-date/to/$end", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "INVALID_DATE")
    }

    "fail when invalid to date value sent" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.endOfYearStatementReadyToBeFinalised(nino, start, end)
        .when()
        .post(s"/ni/$nino/self-employments/abc/end-of-period-statements/from/$start/to/not-a-date", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "INVALID_DATE")
    }

    "fail when the to date is before the from date" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.endOfYearStatementReadyToBeFinalised(nino, end, start)
        .when()
        .post(s"/ni/$nino/self-employments/abc/end-of-period-statements/from/$end/to/$start", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "INVALID_DATE_RANGE")
    }

    "succeed when the from date and to date are on the same day" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.endOfYearStatementReadyToBeFinalised(nino, start, start)
        .when()
        .post(s"/ni/$nino/self-employments/abc/end-of-period-statements/from/$start/to/$start", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(204)
    }

    "fail when submitted early" in {
      val lateEnd = new LocalDate().plusYears(1)

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.endOfYearStatementReadyToBeFinalised(nino, start, lateEnd)
        .when()
        .post(s"/ni/$nino/self-employments/abc/end-of-period-statements/from/$start/to/$lateEnd", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "EARLY_SUBMISSION")
    }

  }

}
