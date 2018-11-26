package uk.gov.hmrc.r2.selfassessmentapi

import uk.gov.hmrc.r2.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.support.BaseFunctionalSpec
import uk.gov.hmrc.r2.selfassessmentapi.config.EmptyResponseFilter

class EmptyResponseFilterSpec extends BaseFunctionalSpec {

  "Empty response filter should" should {
    "be applied when returning an HTTP 201 e.g.: creating a self-employment" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment())
        .to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader(EmptyResponseFilter.emptyHeader, "true".r)
    }

    "be applied when returning an HTTP 409 e.g.: attempting to create a properties business more than once" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.willConflict(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(409)
        .responseContainsHeader(EmptyResponseFilter.emptyHeader, "true".r)
    }

  }
}
