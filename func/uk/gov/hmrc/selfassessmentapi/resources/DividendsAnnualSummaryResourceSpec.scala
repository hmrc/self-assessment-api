package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.Json
import uk.gov.hmrc.support.BaseFunctionalSpec

class DividendsAnnualSummaryResourceSpec extends BaseFunctionalSpec {
  "update annual summary" should {
    "return code 204 when updating dividends" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource
        .when()
        .put(Jsons.Dividends(500)).at(s"/ni/$nino/dividends/$taxYear")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"/ni/$nino/dividends/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Dividends(500).toString)
        .when()
        .put(Jsons.Dividends(200.25)).at(s"/ni/$nino/dividends/$taxYear")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"/ni/$nino/dividends/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Dividends(200.25).toString)
        .when()
        .put(Json.obj()).at(s"/ni/$nino/dividends/$taxYear")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"/ni/$nino/dividends/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Json.obj().toString)
    }

    "return code 400 when updating dividends with an invalid value" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource
        .when()
        .put(Jsons.Dividends(-50.123)).at(s"/ni/$nino/dividends/$taxYear")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.invalidRequest("INVALID_MONETARY_AMOUNT" -> "/ukDividends"))
    }
  }

  "retrieve annual summary" should {
    "return code 200 with empty json when retrieving a dividends annual summary that does not exist" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/dividends/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .jsonBodyIsEmptyObject()
    }
  }
}
