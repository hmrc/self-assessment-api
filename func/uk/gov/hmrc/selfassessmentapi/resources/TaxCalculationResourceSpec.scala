package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.resources.Jsons.TaxCalculation.eta
import uk.gov.hmrc.selfassessmentapi.resources.Jsons.Errors.invalidRequest
import uk.gov.hmrc.support.BaseFunctionalSpec

class TaxCalculationResourceSpec extends BaseFunctionalSpec {
  "requestCalculation" should {

    "return 202 containing a Location header, along with an ETA for the calculation to be ready" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().taxCalculation.isAcceptedFor(nino)
        .when()
        .post(Jsons.TaxCalculation.request()).to(s"/ni/$nino/calculations")
        .thenAssertThat()
        .statusIs(202)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/calculations/\\w+".r)
        .bodyIsLike(eta(5).toString())
    }

    "return 400 when attempting to request calculation without specifying a tax year" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Json.obj()).to(s"/ni/$nino/calculations")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(invalidRequest("MANDATORY_FIELD_MISSING" -> "/taxYear"))
    }

    "return 400 when attempting to request calculation with invalid tax year" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.TaxCalculation.request("2011-12")).to(s"/ni/$nino/calculations")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(invalidRequest("TAX_YEAR_INVALID" -> "/taxYear"))
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().isATeapotFor(nino)
        .when()
        .post(Jsons.TaxCalculation.request()).to(s"/ni/$nino/calculations")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }
  }

  "retrieveCalculation" should {
    "return 200 containing a calculation" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().taxCalculation.isReadyFor(nino)
        .when()
        .get(s"/ni/$nino/calculations/abc")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.TaxCalculation().toString)
    }

    "return 204 when the calculation is not ready" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().taxCalculation.isNotReadyFor(nino)
        .when()
        .get(s"/ni/$nino/calculations/abc")
        .thenAssertThat()
        .statusIs(204)
    }

    "return 404 when provided with an invalid calculation ID" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().taxCalculation.invalidCalculationIdFor(nino)
        .when()
        .get(s"/ni/$nino/calculations/abc")
        .thenAssertThat()
        .statusIs(404)
    }

    "return 404 when attempting to retrieve a calculation using an invalid id" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().taxCalculation.doesNotExistFor(nino)
        .when()
        .get(s"/ni/$nino/calculations/abc")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().isATeapotFor(nino)
        .when()
        .get(s"/ni/$nino/calculations/abc")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }
  }
}
