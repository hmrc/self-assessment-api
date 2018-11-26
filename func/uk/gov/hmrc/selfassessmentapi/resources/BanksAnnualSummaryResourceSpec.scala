package uk.gov.hmrc.r2.selfassessmentapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class BanksAnnualSummaryResourceFuncSpec extends BaseFunctionalSpec {
  "updateAnnualSummary" should {
    "return code 204 when successfully updating a bank annual summary" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(Jsons.Banks()).to(s"/ni/$nino/savings-accounts")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.Banks.annualSummary(Some(50), Some(12.55))).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Banks.annualSummary(Some(50), Some(12.55)).toString)
        .when()
        .put(Jsons.Banks.annualSummary(None, None)).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 400 when attempting to update a bank annual summary with invalid JSON" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(Jsons.Banks()).to(s"/ni/$nino/savings-accounts")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.Banks.annualSummary(Some(50.123), Some(12.555))).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.invalidRequest(
          "INVALID_MONETARY_AMOUNT" -> "/taxedUkInterest",
          "INVALID_MONETARY_AMOUNT" -> "/untaxedUkInterest"))
    }

    "return code 404 when attempting to update a bank annual summary for a non-existent bank" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .put(Jsons.Banks.annualSummary(Some(50.123), Some(12.552))).at(s"/ni/$nino/savings-accounts/sillyid/$taxYear")
        .thenAssertThat()
        .statusIs(400)
    }
  }

  "retrieveAnnualSummary" should {
    "return code 200 for an annual summary that exists" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(Jsons.Banks()).to(s"/ni/$nino/savings-accounts")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.Banks.annualSummary(Some(500.25), Some(22.21))).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Banks.annualSummary(Some(500.25), Some(22.21)).toString)
    }

    "return code 200 with empty json when retrieving a banks annual summary that does not exist" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(Jsons.Banks()).to(s"/ni/$nino/savings-accounts")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .jsonBodyIsEmptyObject()
    }

    "return code 404 when attempting to access an annual summary for a banks source that does not exist" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/savings-accounts/sillyid/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
