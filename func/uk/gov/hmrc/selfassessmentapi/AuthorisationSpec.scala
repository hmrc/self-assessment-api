package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.support.BaseFunctionalSpec

class AuthorisationSpec extends BaseFunctionalSpec {

  "if the user is not authorised they" should {
    "receive 401" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsNotAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(401)
        .contentTypeIsJson()
    }
  }

  "if the user is authorised for the resource as a client or fully-authorised agent they" should {
    "receive 200" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
    }
  }

  "if the user is authorised as a filing-only agent they" should {
    "be able to make POST requests" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsPartiallyAuthorisedForTheResource(nino)
        .des().selfEmployment.willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsJson()
    }

    "be able to make PUT requests" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsPartiallyAuthorisedForTheResource(nino)
        .des().selfEmployment.willBeUpdatedFor(nino)
        .when()
        .put(Jsons.SelfEmployment.update()).at(s"/ni/$nino/self-employments/abc")
        .thenAssertThat()
        .statusIs(204)
    }

    "be forbidden from making GET requests" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsPartiallyAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc")
        .thenAssertThat()
        .statusIs(401)
        .contentTypeIsJson()
    }
  }
}
