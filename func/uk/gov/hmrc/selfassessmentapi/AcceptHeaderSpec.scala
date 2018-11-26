package uk.gov.hmrc.r2.selfassessmentapi

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.support.BaseFunctionalSpec

class AcceptHeaderSpec extends BaseFunctionalSpec {
  val selfEmploymentId: String = BSONObjectID.generate.stringify

  "if the valid content type header is sent in the request, they" should {
    "receive 200" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.noneFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments").withAcceptHeader()
        .thenAssertThat().statusIs(200).jsonBodyIsEmptyArray()
    }
  }

  "if the valid content type header is missing in the request, they" should {
    "receive 406" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.willBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments").withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(406)
        .bodyIsError("ACCEPT_HEADER_INVALID")
    }
  }

}
