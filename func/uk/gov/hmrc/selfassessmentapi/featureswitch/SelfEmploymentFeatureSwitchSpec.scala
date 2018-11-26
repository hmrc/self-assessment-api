package uk.gov.hmrc.r2.selfassessmentapi.featureswitch

import play.api.test.FakeApplication
import uk.gov.hmrc.r2.selfassessmentapi.models.{SourceId, SourceType}
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentFeatureSwitchSpec extends BaseFunctionalSpec {

  private val conf: Map[String, Map[SourceId, Map[SourceId, Map[SourceId, Any]]]] =
    Map("Test" ->
      Map("feature-switch" ->
        Map("self-employments" ->
          Map("enabled" -> false)
        )
      )
    )

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = conf)

  "self-employments" should {
    "not be visible if feature Switched Off" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/${SourceType.SelfEmployments.toString}")
        .thenAssertThat()
        .statusIs(404)
    }
  }

}


