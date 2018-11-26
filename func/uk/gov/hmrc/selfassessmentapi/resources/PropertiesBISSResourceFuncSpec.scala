package uk.gov.hmrc.r2.selfassessmentapi.resources

import play.api.http.Status._
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesBISSResourceFuncSpec extends BaseFunctionalSpec {

    "getSummary for Properties BISS" should {
      "return code 200 for a supplied valid data" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().PropertiesBISS.getSummary(nino, taxYear)
          .when()
          .get(s"/ni/$nino/uk-properties/$taxYear/income-summary")
          .thenAssertThat()
          .statusIs(200)
      }

      "return code 400 for a supplied invalid nino" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().PropertiesBISS.getSummaryErrorResponse(nino, taxYear, BAD_REQUEST, DesJsons.Errors.invalidIdValue)
          .when()
          .get(s"/ni/$nino/uk-properties/$taxYear/income-summary")
          .thenAssertThat()
          .statusIs(400)
      }

      "return multiple errors for a supplied invalid request" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().PropertiesBISS.getSummaryErrorResponse(nino, taxYear, BAD_REQUEST, DesJsons.Errors.multipleErrors)
          .when()
          .get(s"/ni/$nino/uk-properties/$taxYear/income-summary")
          .thenAssertThat()
          .statusIs(400)
      }

    }
}
