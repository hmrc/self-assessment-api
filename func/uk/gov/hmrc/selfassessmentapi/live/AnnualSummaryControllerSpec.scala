package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.{Adjustments, Allowances, SelfEmployment}
import uk.gov.hmrc.support.BaseFunctionalSpec

class AnnualSummaryControllerSpec extends BaseFunctionalSpec {
  private val invalidAdjustmentsJson =
    """
      |{
      |  "includedNonTaxableProfits" : 50,
      |  "basisAdjustment": 20.5,
      |  "overlapReliefUsed": 10.5,
      |  "accountingAdjustment": 22.2,
      |  "averagingAdjustment": 15.5,
      |  "lossBroughtForward": 13,
      |  "outstandingBusinessOopsySpelling": 12
      |}
    """.stripMargin

  private val invalidAllowancesJson =
    """
      |{
      |  "annualInvestmentAllowance" : 50,
      |  "capitalAllowanceMainPool": 20.5,
      |  "capitalAllowanceSpecialRatePool": 10.5,
      |  "businessPremisesRenovationAllowance": 22.2,
      |  "enhancedCapitalAllowance": 15.5,
      |  "allowancesOnSaleeeeeeees": 13
      |}
    """.stripMargin

  "update" should {
    "return code 200 when provided with a valid update JSON for adjustments" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsHalJson()
        .when()
        .put(s"/$saUtr/self-employments/%sourceId%/$taxYear/adjustments", Some(Json.toJson(Adjustments.example())))
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .when()
        .put(s"/$saUtr/self-employments/%sourceId%/$taxYear/adjustments", Some(Json.toJson(Adjustments.example())))
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
    }

    "return code 400 when provided with an invalid update JSON for adjustments" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsHalJson()
        .when()
        .put(s"/$saUtr/self-employments/%sourceId%/$taxYear/adjustments", Some(Json.toJson(Adjustments.example())))
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .when()
        .put(s"/$saUtr/self-employments/%sourceId%/$taxYear/adjustments", Some(Json.parse(invalidAdjustmentsJson)))
        .thenAssertThat()
        .statusIs(400)
    }

    "return code 200 when provided with a valid update JSON for allowances" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsHalJson()
        .when()
        .put(s"/$saUtr/self-employments/%sourceId%/$taxYear/allowances", Some(Json.toJson(Allowances.example())))
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .when()
        .put(s"/$saUtr/self-employments/%sourceId%/$taxYear/allowances", Some(Json.toJson(Allowances.example())))
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
    }

    "return code 400 when provided with an invalid update JSON for allowances" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsHalJson()
        .when()
        .put(s"/$saUtr/self-employments/%sourceId%/$taxYear/allowances", Some(Json.toJson(Allowances.example())))
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .when()
        .put(s"/$saUtr/self-employments/%sourceId%/$taxYear/allowances", Some(Json.parse(invalidAllowancesJson)))
        .thenAssertThat()
        .statusIs(400)
    }
  }

  "find" should {
    "return code 200 when requested for a adjustments that exists" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsHalJson()
        .when()
        .put(s"/$saUtr/self-employments/%sourceId%/$taxYear/adjustments", Some(Json.toJson(Adjustments.example())))
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .when()
        .get(s"/$saUtr/self-employments/%sourceId%/$taxYear/adjustments")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Json.toJson(Adjustments.example()).toString)
    }

    "return code 404 when requested for a adjustments that does not exist" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsHalJson()
        .when()
        .get(s"/$saUtr/self-employments/%sourceId%/$taxYear/adjustments")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 200 when requested for a allowance that exists" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsHalJson()
        .when()
        .put(s"/$saUtr/self-employments/%sourceId%/$taxYear/allowances", Some(Json.toJson(Allowances.example())))
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .when()
        .get(s"/$saUtr/self-employments/%sourceId%/$taxYear/allowances")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyIsLike(Json.toJson(Allowances.example()).toString)
    }

    "return code 404 when requested for a allowance that does not exist" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/$saUtr/self-employments/%sourceId%/$taxYear/allowances")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
