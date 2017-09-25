package uk.gov.hmrc.selfassessmentapi

import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.support.BaseFunctionalSpec

class AuthorisationServiceSpec extends BaseFunctionalSpec {

  private val conf = Map("Test.microservice.services.auth.enabled" -> true)

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = conf)

  "a user" should {

    "receive 403 if the are not subscribed to MTD" in {
      given()
        .userIsNotSubscribedToMtdFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsLike(Jsons.Errors.clientNotSubscribed)
    }

    "receive 500 is returned if the DES business lookup service returns 500" in {
      given()
        .businessDetailsLookupReturns503Error(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "receive 500 is returned if the DES business lookup service returns 503" in {
      given()
        .businessDetailsLookupReturns500Error(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "receive 403 if they are not authorised to access the resource as a client (i.e. not a filing-only agent)" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsNotAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsLike(Jsons.Errors.clientNotSubscribed)
    }

    "receive 403 if they are not authorised to access the resource as a filing-only agent" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsNotPartiallyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsLike(Jsons.Errors.agentNotSubscribed)
    }

    "receive 403 if the bearer token is missing" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .missingBearerToken
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsLike(Jsons.Errors.unauthorised)
    }

    "receive 403 if an upstream 502 error with 'Unable to decrypt value message' is returned" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .upstream502BearerTokenDecryptionError
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsLike(Jsons.Errors.unauthorised)
    }

    "receive 500 if an upstream 5xx error is returned" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .upstream5xxError
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "receive 500 if an upstream 4xx error is returned" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .upstream4xxError
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "receive 500 if an upstream non-fatal exception error is returned" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .upstreamNonFatal
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "receive 200 if the user is authorised for the resource as a client" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
    }

    "receive 200 if the user is authorised for the resource as a fully-authorised agent" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .agentIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
    }

    "receive 200 if the user is authorised for the resource as a fully-authorised agent but could not retrieve agent code" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .agentIsFullyAuthorisedForTheResourceNoAgentCode
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
    }

  }

  "if the user is authorised as a filing-only agent they" should {
    "be able to make POST requests" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsPartiallyAuthorisedForTheResource
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
        .userIsPartiallyAuthorisedForTheResource
        .des().selfEmployment.willBeUpdatedFor(nino)
        .when()
        .put(Jsons.SelfEmployment.update()).at(s"/ni/$nino/self-employments/abc")
        .thenAssertThat()
        .statusIs(204)
    }

    "be forbidden from making GET requests" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsPartiallyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/self-employments/abc")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsLike(Jsons.Errors.agentNotAuthorised)
    }

    "be able to make POST requests but don't have an agentCode" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsPartiallyAuthorisedForTheResourceNoAgentCode
        .des().selfEmployment.willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsJson()
    }
  }

}
