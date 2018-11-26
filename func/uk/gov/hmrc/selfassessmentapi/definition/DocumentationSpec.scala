package uk.gov.hmrc.r2.selfassessmentapi.definition

import uk.gov.hmrc.support.BaseFunctionalSpec

class DocumentationSpec extends BaseFunctionalSpec {


  "Request to /api/definition" should {
    "return 404" in {
      given()
        .when()
        .get("/api/definition")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "Request to /api/conf/1.0/application.raml" should {
    "return 404" in {
      given()
        .when()
        .get("/api/conf/1.0/application.raml")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
