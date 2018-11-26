package uk.gov.hmrc.r2.selfassessmentapi.featureswitch

import play.api.libs.json.Json
import play.api.test.FakeApplication
import uk.gov.hmrc.r2.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.r2.selfassessmentapi.models.selfemployment.SelfEmploymentAnnualSummary
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
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.willBeReturnedFor(nino)
        .when()
        .put(Json.toJson(SelfEmploymentAnnualSummary(None, None, None))).at(s"/ni/$nino/self-employments/abc/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }
  }

}


