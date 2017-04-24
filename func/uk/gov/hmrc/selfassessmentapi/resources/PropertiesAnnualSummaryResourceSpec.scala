package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesAnnualSummaryResourceSpec extends BaseFunctionalSpec {

  "amending annual summaries" should {
    "return code 204 when amending annual summaries" in {
      val property = Jsons.Properties()

      val annualSummaries = Jsons.Properties.otherAnnualSummary(
        annualInvestmentAllowance = 10000.50,
        businessPremisesRenovationAllowance = 500.50,
        otherCapitalAllowance = 1000.20,
        zeroEmissionsGoodsVehicleAllowance = 50.50,
        costOfReplacingDomesticItems = 150.55,
        lossBroughtForward = 20.22,
        privateUseAdjustment = 22.23,
        balancingCharge = 350.34)

      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(annualSummaries).at(s"/ni/$nino/uk-properties/other/$taxYear")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 400 when amending annual summaries with invalid data" in {
      val property = Jsons.Properties()

      val annualSummaries = Jsons.Properties.otherAnnualSummary(
        annualInvestmentAllowance = 10000.50,
        businessPremisesRenovationAllowance = -500.50,
        otherCapitalAllowance = 1000.20,
        zeroEmissionsGoodsVehicleAllowance = 50.50,
        costOfReplacingDomesticItems = 150.55,
        lossBroughtForward = 20.22,
        privateUseAdjustment = -22.23,
        balancingCharge = 350.34)

      val expectedJson = Jsons.Errors.invalidRequest(
        "INVALID_MONETARY_AMOUNT" -> "/allowances/businessPremisesRenovationAllowance",
        "INVALID_MONETARY_AMOUNT" -> "/adjustments/privateUseAdjustment")

      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(annualSummaries).at(s"/ni/$nino/uk-properties/other/$taxYear")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson.toString)
    }

    "return code 404 when amending annual summaries for a properties business that does not exist" in {
      val annualSummaries = Jsons.Properties.otherAnnualSummary(
        annualInvestmentAllowance = 10000.50,
        businessPremisesRenovationAllowance = 500.50,
        otherCapitalAllowance = 1000.20,
        zeroEmissionsGoodsVehicleAllowance = 50.50,
        costOfReplacingDomesticItems = 150.55,
        lossBroughtForward = 20.22,
        privateUseAdjustment = 22.23,
        balancingCharge = 350.34)

      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .when()
        .put(annualSummaries).at(s"/ni/$nino/uk-properties/other/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 404 when attempting to update annual summaries for an invalid property type" in {
      val property = Jsons.Properties()

      val annualSummaries = Jsons.Properties.otherAnnualSummary(
        annualInvestmentAllowance = 10000.50,
        businessPremisesRenovationAllowance = 500.50,
        otherCapitalAllowance = 1000.20,
        zeroEmissionsGoodsVehicleAllowance = 50.50,
        costOfReplacingDomesticItems = 150.55,
        lossBroughtForward = 20.22,
        privateUseAdjustment = 22.23,
        balancingCharge = 350.34)

      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(annualSummaries).at(s"/ni/$nino/uk-properties/silly/$taxYear")
        .thenAssertThat()
        .statusIs(404)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.notFound)
    }
  }

  "retrieving annual summaries" should {
    "return code 200 containing annual summary information" in {
      val property = Jsons.Properties()

      val annualSummaries = Jsons.Properties.otherAnnualSummary(
        annualInvestmentAllowance = 10000.50,
        businessPremisesRenovationAllowance = 500.50,
        otherCapitalAllowance = 1000.20,
        zeroEmissionsGoodsVehicleAllowance = 50.50,
        costOfReplacingDomesticItems = 150.55,
        lossBroughtForward = 20.22,
        privateUseAdjustment = 22.23,
        balancingCharge = 350.34)

      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(annualSummaries).at(s"/ni/$nino/uk-properties/other/$taxYear")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"/ni/$nino/uk-properties/other/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(annualSummaries.toString)
    }

    "return code 200 containing an empty object for an annual summary that is empty" in {
      val property = Jsons.Properties()

      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/uk-properties/other/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .jsonBodyIsEmptyObject()
    }

    "return code 404 when retrieving annual summaries for a properties business that does not exist" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/uk-properties/other/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when retrieving properties annual summary for a non MTD year" in {
      val property = Jsons.Properties()

      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .when()
        .post(property).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/uk-properties/other/2015-16")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsError("TAX_YEAR_INVALID")
    }
  }
}
