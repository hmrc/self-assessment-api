package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class CrystallisationResourceSpec extends BaseFunctionalSpec {
  "intentToCrystallise" should {

    "return 303 containing a Location header" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().crystallisation.intentToCrystallise(nino)
        .des().taxCalculation.isReadyFor(nino, calcId = "77427777")
        .when()
        .post(Jsons.Crystallisation.intentToCrystallise()).to(s"/ni/$nino/$taxYear/intent-to-crystallise")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.TaxCalculation().toString)
    }

    "return 400 when attempting to request calculation with invalid tax year" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(Jsons.Crystallisation.intentToCrystallise()).to(s"/ni/$nino/2011-12/intent-to-crystallise")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsError("TAX_YEAR_INVALID")
    }

    "return 403 when Required End of Period Statement is not submitted" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().crystallisation.requiredEndOfPeriodStatement(nino, taxYear)
        .when()
        .post(Jsons.Crystallisation.intentToCrystallise()).to(s"/ni/$nino/$taxYear/intent-to-crystallise")
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\code", "BUSINESS_ERROR")
        .bodyHasPath("\\errors(0)\\code", "REQUIRED_END_OF_PERIOD_STATEMENT")
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().isATeapotFor(nino)
        .when()
        .post(Jsons.Crystallisation.intentToCrystallise()).to(s"/ni/$nino/$taxYear/intent-to-crystallise")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

  }

}
