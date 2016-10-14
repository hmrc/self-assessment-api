package uk.gov.hmrc.selfassessmentapi.definition

import play.api.libs.json.JsObject
import play.api.test.FakeApplication
import play.utils.UriEncoding
import uk.gov.hmrc.selfassessmentapi.controllers.definition.{APIStatus, SelfAssessmentApiDefinition}
import uk.gov.hmrc.support.BaseFunctionalSpec

class DocumentationSpec extends BaseFunctionalSpec {

  override lazy val app = FakeApplication(additionalConfiguration = Map(
    "Test.white-listing.enabled" -> false,
    "Test.feature-switch.self-employments.enabled" -> true,
    "Test.feature-switch.unearned-incomes.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.uk.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.eea.enabled" -> true,
    "Test.feature-switch.employments.enabled" -> true,
    "Test.feature-switch.uk-properties.enabled" -> true,
    "Test.feature-switch.bank.enabled" -> true,

    "Test.feature-switch.blindPerson.enabled" -> true,
    "Test.feature-switch.charitableGivings.enabled" -> true,
    "Test.feature-switch.childBenefit.enabled" -> true,
    "Test.feature-switch.pensionContributions.enabled" -> true,
    "Test.feature-switch.studentLoan.enabled" -> true,
    "Test.feature-switch.taxRefundedOrSetOff.enabled" -> true))

  "Request to /api/definition" should {
    "return 200 with json response" in {
      given()
        .when()
        .get("/api/definition").withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
    }
  }

  "When white-listing is disabled Request to /api/definition" should {
    "return 200 without the white-listing access section in the json response" in {
      given()
        .when()
        .get("/api/definition").withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyDoesNotHavePath[JsObject](s""" \\ api \\ versions(0) \\ access """)
    }
  }


  "Request to /api/documentation/<version>/<endpoint>" should {
    "return 200 with xml response for all endpoints in SelfAssessmentDefinition" in {
      val definition = new SelfAssessmentApiDefinition("self-assessment", APIStatus.PROTOTYPED).definition
      definition.api.versions foreach { version =>
        version.endpoints foreach { endpoint =>
          val nameInUrl = UriEncoding.encodePathSegment(endpoint.endpointName, "UTF-8")
          given()
            .when()
            .get(s"/api/documentation/${version.version}/$nameInUrl").withoutAcceptHeader()
            .thenAssertThat()
            .statusIs(200)
            .contentTypeIsXml()
        }
      }
    }
  }

}
