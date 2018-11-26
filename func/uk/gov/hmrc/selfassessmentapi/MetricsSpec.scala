package uk.gov.hmrc.r2.selfassessmentapi

import uk.gov.hmrc.support.BaseFunctionalSpec

class MetricsSpec extends BaseFunctionalSpec {

  "Request to /admin/metrics" should {
    "return 200" in {
      given()
        .when()
        .get("/admin/metrics").withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
    }
  }

}
