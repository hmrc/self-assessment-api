package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.support.BaseFunctionalSpec

class AuthorisationSpec extends BaseFunctionalSpec {

  "if the user is not authorised for the resource they" should {
    "receive 401" in {
      given()
        .userIsNotAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(401)
        .contentTypeIsJson()
    }
  }

  "if the user is authorised for the resource they" should {
    "receive 200" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
    }
  }
}
