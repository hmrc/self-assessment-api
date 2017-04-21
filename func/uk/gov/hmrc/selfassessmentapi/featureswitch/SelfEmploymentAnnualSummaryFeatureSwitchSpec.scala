package uk.gov.hmrc.selfassessmentapi.featureswitch

import play.api.libs.json.Json
import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmploymentAnnualSummary
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentAnnualSummaryFeatureSwitchSpec extends BaseFunctionalSpec {

  private val conf: Map[String, _] =
    Map("Test.feature-switch.self-employments" ->
      Map("enabled" -> true, "annual" -> Map("enabled" -> false), "periods" -> Map("enabled" -> true))
    )

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = conf)

  "self-employments" should {
    "not be visible if feature Switched Off" in {

      given()
        .userIsFullyAuthorisedForTheResource(nino)
        .des().selfEmployment.willBeReturnedFor(nino)
        .when()
        .put(Json.toJson(SelfEmploymentAnnualSummary(None, None))).at(s"/ni/$nino/self-employments/abc/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }
  }

}


