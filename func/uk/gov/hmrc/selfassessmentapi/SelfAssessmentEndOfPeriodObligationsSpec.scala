package uk.gov.hmrc.selfassessmentapi

import org.joda.time.LocalDate
import uk.gov.hmrc.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfAssessmentEndOfPeriodObligationsSpec extends BaseFunctionalSpec {

  "Retrieving end-of-period statement obligations" should {

    val from = new LocalDate(2017, 1, 1)
    val to = new LocalDate(2017, 12, 31)

    val testRefNo = "abc"

    "return code 200 with a set of obligations" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.returnEndOfPeriodObligationsFor(nino, testRefNo)
        .when()
        .get(s"/ni/$nino/self-employments/$testRefNo/end-of-period-statements/obligations?from=$from&to=$to")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations.eops(secondMet = true, thirdMet = true, fourthMet = true).toString)
    }

    "return code 400 when from date is invalid" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.returnEndOfPeriodObligationsFor(nino, testRefNo)
        .when()
        .get(s"/ni/$nino/self-employments/$testRefNo/end-of-period-statements/obligations?from=ABC&to=$to")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsError("INVALID_DATE")
    }


    "return code 400 when to date is invalid" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.returnEndOfPeriodObligationsFor(nino, testRefNo)
        .when()
        .get(s"/ni/$nino/self-employments/$testRefNo/end-of-period-statements/obligations?from=$from&to=ABC")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsError("INVALID_DATE")
    }


    "return code 400 when from and to date range is invalid" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.returnEndOfPeriodObligationsFor(nino, testRefNo)
        .when()
        .get(s"/ni/$nino/self-employments/$testRefNo/end-of-period-statements/obligations?from=$from&to=2016-12-31")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsError("INVALID_DATE_RANGE")
    }

  }

}
