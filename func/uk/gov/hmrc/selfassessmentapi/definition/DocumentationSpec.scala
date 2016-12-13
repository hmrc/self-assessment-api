package uk.gov.hmrc.selfassessmentapi.definition

import uk.gov.hmrc.support.BaseFunctionalSpec

class DocumentationSpec extends BaseFunctionalSpec {


  "Request to /api/definition" should {
    "return 200 with json response" in {
      given()
        .when()
        .get("/api/definition").withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
    }
  }

  "Request to /api/conf/1.0/application.raml" should {
    "return 200 with raml response" in {
      given()
        .when()
        .get("/api/conf/1.0/application.raml").withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
    }
  }


}
