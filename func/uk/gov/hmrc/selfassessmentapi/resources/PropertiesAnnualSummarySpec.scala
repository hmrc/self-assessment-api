package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.Json
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesAnnualSummarySpec extends BaseFunctionalSpec {

  "create or update annual summary" should {
    "return code 204 if the create/update is successful" ignore {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(Jsons.Properties.annualSummary()).at(s"/ni/$nino/properties/uk/$taxYear")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"/ni/$nino/properties/uk/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Properties.annualSummary().toString())
        .when()
        .put(Jsons.Properties.annualSummary(annualInvestmentAllowance = 100.12, balancingCharge = 100.12)).at(s"/ni/$nino/properties/uk/$taxYear")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"/ni/$nino/properties/uk/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Properties.annualSummary(annualInvestmentAllowance = 100.12, balancingCharge = 100.12).toString())
    }

    "return code 400 when provided with an invalid annual summary" ignore {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(Jsons.Properties.annualSummary(annualInvestmentAllowance = -100.00, rentARoomRelief = -10.0)).at(s"/ni/$nino/properties/uk/$taxYear")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_MONETARY_AMOUNT", "/allowances/annualInvestmentAllowance"),
          ("INVALID_MONETARY_AMOUNT", "/rentARoomRelief")))
    }

    "can be set to an empty object" ignore {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(Json.parse("{}")).at(s"/ni/$nino/properties/uk/$taxYear")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"/ni/$nino/properties/uk/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIs("{}")
    }
  }

  "retrieveAnnualSummary" should {
    "return code 404 when retrieving a non-existent annual summary" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/properties/uk/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
