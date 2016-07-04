package uk.gov.hmrc.selfassessmentapi.live

import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.support.BaseFunctionalSpec

class TaxYearHalLinksSpec extends BaseFunctionalSpec {

  private val conf: Map[String, Any] = Map("Test.feature-switch.self-employments.enabled" -> false)

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = conf)

  "Request to discover tax year" should {
    "have Hal links for self-employments" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyDoesNotHaveLinksForSourceType(SelfEmployments, saUtr, taxYear)
    }
  }

}
