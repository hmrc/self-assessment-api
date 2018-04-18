package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesObligationsResourceSpec extends BaseFunctionalSpec {
  "retrieveObligations" ignore {
    "return code 200 containing a set of canned obligations, all of which have not been met" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.willBeCreatedFor(nino)
        .des().properties.returnObligationsFor(nino)
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

    "return code 200 containing a set of canned obligations of which only the first has been met" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.willBeCreatedFor(nino)
        .des().properties.returnObligationsFor(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/uk-properties/obligations").withHeaders(GovTestScenarioHeader, "FIRST_MET")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations(firstMet = true).toString)
    }

    "return code 200 containing a set of canned obligations, all of which have been met" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.willBeCreatedFor(nino)
        .des().properties.returnObligationsFor(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/uk-properties/obligations").withHeaders(GovTestScenarioHeader, "ALL_MET")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations(firstMet = true, secondMet = true, thirdMet = true, fourthMet = true).toString)
    }

//    "do something when a set of obligations is returned without an identification section" in {
//      given()
//        .userIsSubscribedToMtdFor(nino)
//        .clientIsFullyAuthorisedForTheResource
//        .des().properties.willBeCreatedFor(nino)
//        .des().properties.returnObligationsFor(nino)
//        .when()
//        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
//        .thenAssertThat()
//        .statusIs(201)
//        .when()
//        .get(s"/ni/$nino/uk-properties/obligations").withHeaders(GovTestScenarioHeader, "ALL_MET")
//        .thenAssertThat()
//        .statusIs(200)
//        .bodyIsLike(Jsons.Obligations(firstMet = true, secondMet = true, thirdMet = true, fourthMet = true).toString)
//    }

    "return code 404 when attempting to retrieve obligations for a properties business that does not exist" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/uk-properties/obligations")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
