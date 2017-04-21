package uk.gov.hmrc.selfassessmentapi.featureswitch

import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentPeriodicSummaryFeatureSwitchSpec extends BaseFunctionalSpec {

  private val conf: Map[String, _] =
    Map("Test" ->
      Map("feature-switch" ->
        Map("self-employments" ->
          Map("enabled" -> true, "annual" -> Map("enabled" -> true), "periods" -> Map("enabled" -> false))
        )
      )
    )

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = conf)

  "self-employments" should {
    "not be visible if feature Switched Off" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .des().selfEmployment.willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(404)
    }
  }

}


