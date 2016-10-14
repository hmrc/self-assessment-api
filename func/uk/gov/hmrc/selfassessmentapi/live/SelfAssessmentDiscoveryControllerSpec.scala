package uk.gov.hmrc.selfassessmentapi.live

import play.api.test.FakeApplication
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfAssessmentDiscoveryControllerSpec extends BaseFunctionalSpec {

  override lazy val app = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.self-employments.enabled" -> true,
    "Test.feature-switch.unearned-incomes.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.uk.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.eea.enabled" -> true,
    "Test.feature-switch.employments.enabled" -> true,
    "Test.feature-switch.banks.enabled" -> true,
    "Test.feature-switch.uk-properties.enabled" -> true))

  "Live tax years discovery" should {
    "return a 200 response with links" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr")
        .bodyHasLink(taxYear, s"/self-assessment/$saUtr/$taxYear")
    }
  }

  "Live tax year discovery" should {
    "return a 200 response status" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"""/self-assessment/$saUtr/$taxYear""")
        .bodyHasLink("self-employments", s"""/self-assessment/$saUtr/$taxYear/self-employments""")
    }
  }

}
