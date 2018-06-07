package uk.gov.hmrc.selfassessmentapi.resources

import play.api.http.Status._
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentBISSResourceFuncSpec extends BaseFunctionalSpec {

  val seId = "test-source-id"

    "getSummary for SE BISS" should {
      "return code 200 for a supplied valid data" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().SelfEmploymentBISS.getSummary(nino, taxYear, seId)
          .when()
          .get(s"/ni/$nino/self-employments/$seId/$taxYear/income-summary")
          .thenAssertThat()
          .statusIs(200)
      }

      "return code 400 for a supplied invalid nino" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().SelfEmploymentBISS.getSummaryErrorResponse(nino, taxYear, seId, BAD_REQUEST, DesJsons.Errors.invalidNino)
          .when()
          .get(s"/ni/$nino/self-employments/$seId/$taxYear/income-summary")
          .thenAssertThat()
          .statusIs(400)
      }
    }
}
