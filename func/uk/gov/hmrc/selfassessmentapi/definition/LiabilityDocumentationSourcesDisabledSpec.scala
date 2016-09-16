package uk.gov.hmrc.selfassessmentapi.definition

import play.api.test.FakeApplication
import play.utils.UriEncoding
import uk.gov.hmrc.selfassessmentapi.controllers.definition.{APIStatus, SelfAssessmentApiDefinition}
import uk.gov.hmrc.support.BaseFunctionalSpec

class LiabilityDocumentationSourcesDisabledSpec extends BaseFunctionalSpec {

  override lazy val app = FakeApplication(additionalConfiguration =  Map(
    "Test.feature-switch.sources.employments.enabled" -> false,
    "Test.feature-switch.sources.self-employments.enabled" -> false,
    "Test.feature-switch.sources.unearned-incomes.enabled" -> false,
    "Test.feature-switch.sources.furnished-holiday-lettings.enabled" -> false,
    "Test.feature-switch.sources.uk-properties.enabled" -> false))

  "Request to retrieve liability documentation when all the sources are disabled" should {
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
            .bodyDoesNotHaveString("employment-1")
            .bodyDoesNotHaveString("self-employment-1")
            .bodyDoesNotHaveString("interest-income-1")
            .bodyDoesNotHaveString("dividend-income-1")
        }
      }
    }
  }

}
