package uk.gov.hmrc.r2.selfassessmentapi

import uk.gov.hmrc.support.BaseFunctionalSpec

class HealthSpec extends BaseFunctionalSpec {

  "Request to /ping/ping" should {
    "return 200" in {
      given()
        .when()
        .get("/ping/ping").withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
    }
  }

  "Request to /admin/details" should {
    "return 200" in {
      given()
        .when()
        .get("/admin/details").withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
    }
  }

}
