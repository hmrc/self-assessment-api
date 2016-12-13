package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentAnnualSummarySpec extends BaseFunctionalSpec {

  "updateAnnualSummary" should {
    "return code 204 when updating an annual summary for a valid self-employment source" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.SelfEmployment.annualSummary()).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 404 when updating an annual summary for an invalid self-employment source" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(Jsons.SelfEmployment.annualSummary()).at(s"/ni/$nino/self-employments/sillysource/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when updating an annual summary providing an invalid adjustment & allowance" in {
      val annualSummaries = Jsons.SelfEmployment.annualSummary(
        includedNonTaxableProfits = -100, overlapReliefUsed = -100,
        goodsAndServicesOwnUse = -100, capitalAllowanceMainPool = -100)

      val expectedBody = Jsons.Errors.invalidRequest(
        ("INVALID_MONETARY_AMOUNT", "/adjustments/includedNonTaxableProfits"),
        ("INVALID_MONETARY_AMOUNT", "/adjustments/overlapReliefUsed"),
        ("INVALID_MONETARY_AMOUNT", "/adjustments/goodsAndServicesOwnUse"),
        ("INVALID_MONETARY_AMOUNT", "/allowances/capitalAllowanceMainPool"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(annualSummaries).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }
  }

  "retrieveAnnualSummary" should {
    "return code 200 when retrieving an annual summary that exists" in {
      val annualSummaries = Jsons.SelfEmployment.annualSummary()
      val expectedJson = annualSummaries.toString()

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(annualSummaries).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson)
    }

    "return code 200 containing and empty object when retrieving a non-existent annual summary" in {

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .jsonBodyIsEmptyObject
    }
  }

}
