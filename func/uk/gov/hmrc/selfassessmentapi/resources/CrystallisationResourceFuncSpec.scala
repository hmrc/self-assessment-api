package uk.gov.hmrc.r2.selfassessmentapi.resources

import uk.gov.hmrc.r2.selfassessmentapi.models.Errors._
import uk.gov.hmrc.support.BaseFunctionalSpec

class CrystallisationResourceFuncSpec extends BaseFunctionalSpec {
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

    val calcId = "041f7e4d-87b9-4d4a-a296-3cfbdf92f7e2"
    val crystallisationRequest = Jsons.Crystallisation.crystallisationRequest(calcId)
    "return 201 in happy scenario" in {

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().crystallisation.crystallise(nino, taxYear, calcId)
        .when()
        .post(crystallisationRequest).to(s"/ni/$nino/$taxYear/crystallisation")
        .thenAssertThat()
        .statusIs(201)
    }

    "return 500 when INVALID_IDTYPE is returned from DES" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().crystallisation.crystalliseError(nino, taxYear, calcId)(400, DesJsons.Errors.invalidIdType)
        .when()
        .post(crystallisationRequest).to(s"/ni/$nino/$taxYear/crystallisation")
        .thenAssertThat()
        .statusIs(500)
    }

    "return 400 NINO_INVALID when INVALID_IDVALUE is returned from DES" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().crystallisation.crystalliseError(nino, taxYear, calcId)(400, DesJsons.Errors.invalidIdValue)
        .when()
        .post(crystallisationRequest).to(s"/ni/$nino/$taxYear/crystallisation")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsError(NinoInvalid.code)
    }

    "return 400 TAX_YEAR_INVALID when INVALID_TAXYEAR is returned from DES" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().crystallisation.crystalliseError(nino, taxYear, calcId)(400, DesJsons.Errors.invalidTaxYear)
        .when()
        .post(crystallisationRequest).to(s"/ni/$nino/$taxYear/crystallisation")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsError(TaxYearInvalid.code)
    }

    "return 403 INVALID_TAX_CALCULATION_ID when INVALID_CALCID is returned from DES" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().crystallisation.crystalliseError(nino, taxYear, calcId)(400, DesJsons.Errors.invalidCalcId)
        .when()
        .post(crystallisationRequest).to(s"/ni/$nino/$taxYear/crystallisation")
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\code", "BUSINESS_ERROR")
        .bodyHasPath("\\errors(0)\\code", InvalidTaxCalculationId.code)
    }

    "return 404 when any 404 is returned from DES" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().crystallisation.crystalliseError(nino, taxYear, calcId)(404, "any body")
        .when()
        .post(crystallisationRequest).to(s"/ni/$nino/$taxYear/crystallisation")
        .thenAssertThat()
        .statusIs(404)
    }

    "return 403 REQUIRED_INTENT_TO_CRYSTALLISE when any 409 is returned from DES" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().crystallisation.crystalliseError(nino, taxYear, calcId)(409, "any body")
        .when()
        .post(crystallisationRequest).to(s"/ni/$nino/$taxYear/crystallisation")
        .thenAssertThat()
        .statusIs(403)
        .bodyHasPath("\\code", "BUSINESS_ERROR")
        .bodyHasPath("\\errors(0)\\code", RequiredIntentToCrystallise.code)
    }
  }

}
