package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.{Adjustments, SelfEmployment}
import uk.gov.hmrc.support.BaseFunctionalSpec

class AdjustmentsControllerSpec extends BaseFunctionalSpec {
  private val invalidJson =
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


  "update" should {
    "return code 200 when provided with a valid update JSON" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(s"/$saUtr/$taxYear/self-employments/%sourceId%/adjustments", Some(Json.toJson(Adjustments.example)))
        .thenAssertThat()
        .statusIs(200)
        .when()
        .put(s"/$saUtr/$taxYear/self-employments/%sourceId%/adjustments", Some(Json.toJson(Adjustments.example)))
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
        .put(s"/$saUtr/$taxYear/self-employments/%sourceId%/adjustments", Some(Json.toJson(Adjustments.example)))
        .thenAssertThat()
        .statusIs(200)
        .when()
        .put(s"/$saUtr/$taxYear/self-employments/%sourceId%/adjustments", Some(Json.parse(invalidJson)))
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
        .when()
        .put(s"/$saUtr/$taxYear/self-employments/%sourceId%/adjustments", Some(Json.toJson(Adjustments.example)))
        .thenAssertThat()
        .statusIs(200)
        .when()
        .get(s"/$saUtr/$taxYear/self-employments/%sourceId%/adjustments")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Json.toJson(Adjustments.example).toString)
    }

    "return code 200 when requested for a adjustments that does not exist (i.e. return adjustment with all 0s)" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/$saUtr/$taxYear/self-employments/%sourceId%/adjustments")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Json.toJson(Adjustments(0, 0, 0, 0, 0, 0, 0)).toString)
    }
  }
}
