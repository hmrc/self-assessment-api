package uk.gov.hmrc.selfassessmentapi

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.support.BaseFunctionalSpec

class AuthorisationSpec extends BaseFunctionalSpec {

  val saUtr = generateSaUtr()
  val selfEmploymentId = BSONObjectID.generate.stringify

  "if the user is not authorised for the resource they" should {
    "receive 401" in {
      given()
        .userIsNotAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/self-employments/$selfEmploymentId")
        .thenAssertThat()
        .statusIs(401)
    }
  }

  "if the user is authorised for the resource they" should {
    "receive a 501 response for any live resource" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/self-employments/$selfEmploymentId")
        .thenAssertThat()
        .statusIs(501)
    }
  }
}
