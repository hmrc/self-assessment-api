package uk.gov.hmrc.r2.selfassessmentapi

import play.api.test.FakeApplication
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.PropertyType
import uk.gov.hmrc.r2.selfassessmentapi.resources.Jsons
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
        .des()
        .selfEmployment
        .willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsJson()
    }

    "be able to make PUT requests" in {
      val property = Jsons.Properties.otherPeriod(
        fromDate = Some("2017-04-06"),
        toDate = Some("2018-04-05"),
        rentIncome = 500,
        rentIncomeTaxDeducted = 250.55,
        premiumsOfLeaseGrant = Some(200.22),
        reversePremiums = 22.35,
        premisesRunningCosts = Some(100.50),
        repairsAndMaintenance = Some(100.50),
        financialCosts = Some(100),
        professionalFees = Some(100.50),
        costOfServices = Some(100.50),
        otherCost = Some(100.50),
        residentialFinancialCost = Some(100.55)
      )

      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsPartiallyAuthorisedForTheResource
        .des()
        .properties
        .periodWillBeUpdatedFor(nino, PropertyType.OTHER)
        .when()
        .put(property)
        .at(s"/ni/$nino/uk-properties/${PropertyType.OTHER}/periods/2017-04-06_2018-04-05")
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
        .des()
        .selfEmployment
        .willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .contentTypeIsJson()
    }
  }

}
