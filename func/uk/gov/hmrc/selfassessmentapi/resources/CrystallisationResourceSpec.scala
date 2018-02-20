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
        .des().crystallisation.intentToCrystalliseRequiredEndOfPeriodStatement(nino, taxYear)
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

  "crystallise" should {

    "return 201 in happy scenario" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().crystallisation.crystallise(nino, taxYear)
        .when()
        .post(Jsons.Crystallisation.crystallisation()).to(s"/ni/$nino/$taxYear/crystallisation")
        .thenAssertThat()
        .statusIs(201)
    }

    "return 403 when the tax calculation id is invalid" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().crystallisation.crystalliseInvalidCalculationId(nino, taxYear)
        .when()
        .post(Jsons.Crystallisation.crystallisation()).to(s"/ni/$nino/$taxYear/crystallisation")
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\code", "BUSINESS_ERROR")
        .bodyHasPath("\\errors(0)\\code", "INVALID_TAX_CALCULATION_ID")
        .bodyHasPath("\\errors(0)\\path", "/calculationId")

    }

    "return 403 when not preceded with the intent to crystallise call" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().crystallisation.crystalliseRequiredIntentToCrystallise(nino, taxYear)
        .when()
        .post(Jsons.Crystallisation.crystallisation()).to(s"/ni/$nino/$taxYear/crystallisation")
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\code", "BUSINESS_ERROR")
        .bodyHasPath("\\errors(0)\\code", "REQUIRED_INTENT_TO_CRYSTALLISE")
    }
  }

  "crystallisation obligation information" should {

    "return 200 and the response - the happy scenario" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().crystallisation.crystallisationObligation(nino, taxYear)
        .when()
        .get(s"/ni/$nino/$taxYear/crystallisation/obligation")
        .thenAssertThat()
        .statusIs(200)
        .bodyHasPath("\\start", taxYear.taxYearFromDate.toString)
        .bodyHasPath("\\end", taxYear.taxYearToDate.toString)
        .bodyHasPath("\\due", "2019-01-31")
        .bodyHasPath("\\met", false)
    }

    "return code 400 when nino is invalid" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().crystallisation.crystallisationObligation(nino, taxYear)
        .when()
        .get(s"/ni/invalidNino/$taxYear/crystallisation/obligation")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsError("NINO_INVALID")
    }

    "return 400 when tax year is invalid" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().crystallisation.crystallisationObligation(nino, taxYear)
        .when()
        .get(s"/ni/$nino/2012-11/crystallisation/obligation")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsError("TAX_YEAR_INVALID")
    }
  }
}
