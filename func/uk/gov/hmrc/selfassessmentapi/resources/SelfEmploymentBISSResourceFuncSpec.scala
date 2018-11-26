package uk.gov.hmrc.r2.selfassessmentapi.resources

import play.api.http.Status._
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentBISSResourceFuncSpec extends BaseFunctionalSpec {

  val selfEmploymentId = "XKIS00000000988"

    "getSummary for SE BISS" should {
      "return code 200 for a supplied valid data" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().SelfEmploymentBISS.getSummary(nino, taxYear, selfEmploymentId)
          .when()
          .get(s"/ni/$nino/self-employments/$selfEmploymentId/$taxYear/income-summary")
          .thenAssertThat()
          .statusIs(200)
      }

      "return code 400 for a supplied invalid nino" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().SelfEmploymentBISS.getSummaryErrorResponse(nino, taxYear, selfEmploymentId, BAD_REQUEST, DesJsons.Errors.invalidIdValue)
          .when()
          .get(s"/ni/$nino/self-employments/$selfEmploymentId/$taxYear/income-summary")
          .thenAssertThat()
          .statusIs(400)
      }
    }
}
