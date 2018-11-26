package uk.gov.hmrc.r2.selfassessmentapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesObligationsResourceSpec extends BaseFunctionalSpec {
  "retrieveObligations" should {
    "return code 200 containing a set of canned obligations" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.willBeCreatedFor(nino)
        .des().obligations.returnObligationsFor(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/uk-properties/obligations")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations().toString)
    }

    "return code 404 when attempting to retrieve obligations for a properties business that does not exist" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/uk-properties/obligations")
        .thenAssertThat()
        .statusIs(404)
    }

    "forward the GovTestScenario header to DES" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.receivesObligationsTestHeader(nino, "ALL_MET")
        .when()
        .get(s"/ni/$nino/uk-properties/obligations").withHeaders(GovTestScenarioHeader, "ALL_MET")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations().toString)
    }

    "return code 404 when obligations with no 'identification' data is returned" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.returnObligationsWithNoIdentificationFor(nino)
        .when()
        .get(s"/ni/$nino/uk-properties/obligations")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 404 when property id does not exist" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.obligationNotFoundFor(nino)
        .when()
        .get(s"/ni/$nino/uk-properties/obligations")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when nino is invalid" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().invalidNinoFor(nino)
        .when()
        .get(s"/ni/abc1234/uk-properties/obligations")
        .thenAssertThat()
        .statusIs(400)
    }
  }
}
