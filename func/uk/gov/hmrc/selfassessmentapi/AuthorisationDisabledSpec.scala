package uk.gov.hmrc.r2.selfassessmentapi

import play.api.test.FakeApplication
import uk.gov.hmrc.r2.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.support.BaseFunctionalSpec

class AuthorisationDisabledSpec extends BaseFunctionalSpec {
  private val conf = Map("Test.microservice.services.auth.enabled" -> false)

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = conf)

  "when auth is disabled, any request" should {
    "be allowed" in {
      given()
        .des().selfEmployment.willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
    }
  }
}
