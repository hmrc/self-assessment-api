package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.{Adjustments, SelfEmployment}
import uk.gov.hmrc.support.BaseFunctionalSpec

class AdjustmentsControllerSpec extends BaseFunctionalSpec {
  private val invalidJson =
    """
      |{
      |"hello":23
      |}
    """.stripMargin

  "create" should {
    "return code 201 when provided with a valid JSON" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/adjustments", Some(Json.toJson(Adjustments.example)))
        .thenAssertThat()
        .statusIs(201)
    }

    "return code 400 when provided with an invalid JSON" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/adjustments", Some(Json.parse(invalidJson)))
        .thenAssertThat()
        .statusIs(400)
    }
  }

  "update" should {
    "return code 200 when provided with a valid update JSON" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments", Some(Json.toJson(SelfEmployment.example())))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/adjustments", Some(Json.toJson(Adjustments.example)))
        .thenAssertThat()
        .statusIs(201)
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
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/adjustments", Some(Json.toJson(Adjustments.example)))
        .thenAssertThat()
        .statusIs(201)
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
        .post(s"/$saUtr/$taxYear/self-employments/%sourceId%/adjustments", Some(Json.toJson(Adjustments.example)))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/$saUtr/$taxYear/self-employments/%sourceId%/adjustments")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Json.toJson(Adjustments.example).toString)
    }

    "return code 200 when requested for a adjustments consists of all zeros (i.e. no values have been set)" in {
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
        .bodyIsLike(Json.toJson(Adjustments()).toString)
    }
  }
}
