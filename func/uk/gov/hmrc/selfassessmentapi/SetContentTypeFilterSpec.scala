package uk.gov.hmrc.r2.selfassessmentapi

import uk.gov.hmrc.support.BaseFunctionalSpec

class SetContentTypeFilterSpec extends BaseFunctionalSpec {
  "Set Content Type filter" should {
    "set the content type of the response to application/json" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource
        .des()
        .selfEmployment
        .noContentTypeFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(404)
        .responseContainsHeader("Content-Type", "application/json".r)
    }
  }
}
