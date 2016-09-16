package uk.gov.hmrc.selfassessmentapi.definition

import play.api.test.FakeApplication
import play.utils.UriEncoding
import uk.gov.hmrc.selfassessmentapi.controllers.definition.{APIStatus, SelfAssessmentApiDefinition}
import uk.gov.hmrc.support.BaseFunctionalSpec

class LiabilityDocumentationSourcesEnabledSpec extends BaseFunctionalSpec {

  override lazy val app = FakeApplication(additionalConfiguration =  Map(
    "Test.feature-switch.sources.employments.enabled" -> true,
    "Test.feature-switch.sources.self-employments.enabled" -> true,
    "Test.feature-switch.sources.unearned-incomes.enabled" -> true,
    "Test.feature-switch.sources.furnished-holiday-lettings.enabled" -> true,
    "Test.feature-switch.sources.uk-properties.enabled" -> true))

  "Request to retrieve liability documentation when all the sources are enabled" should {
    "return 200 with xml response with all sources in the xml" in {
      val definition = new SelfAssessmentApiDefinition("self-assessment", APIStatus.PROTOTYPED).definition
      definition.api.versions foreach { version =>
        version.endpoints filter(_.endpointName == "Retrieve Liability") foreach { endpoint =>
          val nameInUrl = UriEncoding.encodePathSegment(endpoint.endpointName, "UTF-8")
          given()
            .when()
            .get(s"/api/documentation/${version.version}/$nameInUrl").withoutAcceptHeader()
            .thenAssertThat()
            .statusIs(200)
            .contentTypeIsXml()
            .bodyHasString("employment-1")
            .bodyHasString("self-employment-1")
            .bodyHasString("interest-income-1")
            .bodyHasString("dividend-income-1")
        }
      }
    }
  }

}
