package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.{Allowances, SelfEmployment}
import uk.gov.hmrc.support.BaseFunctionalSpec

class AllowancesControllerSpec extends BaseFunctionalSpec {
  private val invalidJson =
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
    "return code 200 when provided with a valid update JSON" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(s"/$saUtr/$taxYear/self-employments/%sourceId%/allowances", Some(Json.toJson(Allowances.example)))
        .thenAssertThat()
        .statusIs(200)
        .when()
        .put(s"/$saUtr/$taxYear/self-employments/%sourceId%/allowances", Some(Json.toJson(Allowances.example)))
        .thenAssertThat()
        .statusIs(200)
    }

    "return code 400 when provided with an invalid update JSON" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(s"/$saUtr/$taxYear/self-employments/%sourceId%/allowances", Some(Json.toJson(Allowances.example)))
        .thenAssertThat()
        .statusIs(200)
        .when()
        .put(s"/$saUtr/$taxYear/self-employments/%sourceId%/allowances", Some(Json.parse(invalidJson)))
        .thenAssertThat()
        .statusIs(400)
    }
  }

  "find" should {
    "return code 200 when requested for a allowance that exists" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(s"/$saUtr/$taxYear/self-employments/%sourceId%/allowances", Some(Json.toJson(Allowances.example)))
        .thenAssertThat()
        .statusIs(200)
        .when()
        .get(s"/$saUtr/$taxYear/self-employments/%sourceId%/allowances")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Json.toJson(Allowances.example).toString)
    }

    "return code 200 when requested for a allowance that does not exist (i.e. return allowance with all 0s)" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/$saUtr/$taxYear/self-employments/%sourceId%/allowances")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Json.toJson(Allowances(0, 0, 0, 0, 0, 0)).toString)
    }
  }
}
