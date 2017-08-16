package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentObligationsResourceSpec extends BaseFunctionalSpec {
  "retrieveObligations" should {

    "return code 200 with a set of obligations" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.returnObligationsFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/obligations")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations().toString)
        .when()
        .get("/admin/metrics")
        .thenAssertThat()
        .body(_ \ "timers" \ "Timer-API-SelfEmployments-obligations-GET" \ "count").is(1)
    }

    "forward the GovTestScenario header to DES" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.receivesObligationsTestHeader(nino, "ALL_MET")
        .when()
        .get(s"/ni/$nino/self-employments/abc/obligations").withHeaders(GovTestScenarioHeader, "ALL_MET")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations().toString)
    }

    "return code 404 when self employment id does not exist" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.obligationNotFoundFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/obligations")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when nino is invalid" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().invalidNinoFor(nino)
        .when()
        .get("/ni/abcd1234/self-employments/abc/obligations")
        .thenAssertThat()
        .statusIs(400)
    }
  }
}
