/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.selfassessmentapi

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesPeriodEndOfPeriodStatementSpec extends BaseFunctionalSpec {

  "Submitting your uk-properties end-of-period statement" should {

    val start = new LocalDate(2017, 4, 6)
    val end = new LocalDate(2017, 12, 31)

    "be successful when all pre-requisites have been met" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.endOfYearStatementReadyToBeFinalised(nino, start, end)
        .when()
        .post(s"/ni/$nino/uk-properties/end-of-period-statements/from/$start/to/$end", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(204)
    }

    "fail when the declaration is not finalised" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.endOfYearStatementReadyToBeFinalised(nino, start, end)
        .when()
        .post(s"/ni/$nino/uk-properties/end-of-period-statements/from/$start/to/$end", Some(Json.parse("""{ "finalised": false }""")))
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\errors(0)\\code", "NOT_FINALISED")
        .bodyHasPath("\\errors(0)\\path", "/finalised")
    }

    "fail when there are missing periodic updates from the EOP statement" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.endOfYearStatementMissingPeriod(nino, start, end)
        .when()
        .post(s"/ni/$nino/uk-properties/end-of-period-statements/from/$start/to/$end", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\code", "BUSINESS_ERROR")
        .bodyHasPath("\\errors(0)\\code", "PERIODIC_UPDATE_MISSING")
    }

    "fail when the EOP statement submitted early for the accounting period" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.endOfYearStatementIsEarly(nino, start, end)
        .when()
        .post(s"/ni/$nino/uk-properties/end-of-period-statements/from/$start/to/$end", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\code", "BUSINESS_ERROR")
        .bodyHasPath("\\errors(0)\\code", "EARLY_SUBMISSION")
    }

    "fail when declaration flag isn't set to the request" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.endOfYearStatementReadyToBeFinalised(nino, start, end)
        .when()
        .post(s"/ni/$nino/uk-properties/end-of-period-statements/from/$start/to/$end", Some(Json.parse("""{ "finalised": null }""")))
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\errors(0)\\code", "INVALID_BOOLEAN_VALUE")
        .bodyHasPath("\\errors(0)\\path", "/finalised")
    }

    "fail when invalid from date value sent" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.endOfYearStatementReadyToBeFinalised(nino, start, end)
        .when()
        .post(s"/ni/$nino/uk-properties/end-of-period-statements/from/not-a-date/to/$end", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "INVALID_DATE")
    }

    "fail when invalid to date value sent" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.endOfYearStatementReadyToBeFinalised(nino, start, end)
        .when()
        .post(s"/ni/$nino/uk-properties/end-of-period-statements/from/$start/to/not-a-date", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "INVALID_DATE")
    }

    "fail when the to date is before the from date" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.endOfYearStatementReadyToBeFinalised(nino, end, start)
        .when()
        .post(s"/ni/$nino/uk-properties/end-of-period-statements/from/$end/to/$start", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "INVALID_DATE_RANGE")
    }

    "succeed when the from date and to date are on the same day" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.endOfYearStatementReadyToBeFinalised(nino, start, start)
        .when()
        .post(s"/ni/$nino/uk-properties/end-of-period-statements/from/$start/to/$start", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(204)
    }

    "fail when start date is before April 6th 2017" in {
      val earlyStart = new LocalDate(2017, 4, 4)

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.endOfYearStatementReadyToBeFinalised(nino, earlyStart, end)
        .when()
        .post(s"/ni/$nino/uk-properties/end-of-period-statements/from/$earlyStart/to/$end", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(400)
        .bodyHasPath("\\code", "INVALID_START_DATE")
    }

    "fail when statement period does not match accounting period" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.endOfYearStatementDoesNotMatchPeriod(nino, start, end)
        .when()
        .post(s"/ni/$nino/uk-properties/end-of-period-statements/from/$start/to/$end", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\code", "BUSINESS_ERROR")
        .bodyHasPath("\\errors(0)\\code", "NON_MATCHING_PERIOD")
    }

    "fail when statement has already been submitted" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.endOfYearStatementAlreadySubmitted(nino, start, end)
        .when()
        .post(s"/ni/$nino/uk-properties/end-of-period-statements/from/$start/to/$end", Some(Json.parse("""{ "finalised": true }""")))
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\code", "BUSINESS_ERROR")
        .bodyHasPath("\\errors(0)\\code", "ALREADY_SUBMITTED")
    }

  }

}
