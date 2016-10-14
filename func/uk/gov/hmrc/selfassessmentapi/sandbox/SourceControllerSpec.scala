package uk.gov.hmrc.selfassessmentapi.sandbox

import java.util.UUID

import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.controllers.api.SourceTypes
import uk.gov.hmrc.support.BaseFunctionalSpec

class SourceControllerSpec extends BaseFunctionalSpec {

  override lazy val app = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.self-employments.enabled" -> true,
    "Test.feature-switch.unearned-incomes.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.uk.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.eea.enabled" -> true,
    "Test.feature-switch.employments.enabled" -> true,
    "Test.feature-switch.banks.enabled" -> true,
    "Test.feature-switch.uk-properties.enabled" -> true))

  val sourceId = UUID.randomUUID().toString

  "Sandbox Self source" should {

    "return a 404 error when source type is invalid" in {
      when()
        .get(s"/sandbox/$saUtr/$taxYear/blah")
        .thenAssertThat()
        .isNotFound
  }

    "return a 201 response with links to newly created source" in {
      SourceTypes.types.foreach { sourceType =>
        when()
          .post(s"/sandbox/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
          .thenAssertThat()
          .statusIs(201)
          .contentTypeIsHalJson()
          .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
          .bodyHasSummaryLinks(sourceType, saUtr, taxYear)
      }
    }

    "return a valid response containing an existing source" in {
      SourceTypes.types.foreach { sourceType =>
        when()
          .get(s"/sandbox/$saUtr/$taxYear/${sourceType.name}/$sourceId")
          .thenAssertThat()
          .statusIs(200)
          .contentTypeIsHalJson()
          .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId")
          .bodyHasSummaryLinks(sourceType, sourceId, saUtr, taxYear)
      }
    }

    "return a valid response when retrieving list of sources" in {
      SourceTypes.types.foreach { sourceType =>
        when()
          .get(s"/sandbox/$saUtr/$taxYear/${sourceType.name}")
          .thenAssertThat()
          .statusIs(200)
          .contentTypeIsHalJson()
          .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}")
          .bodyHasPath(s"""_embedded \\ ${sourceType.name}(0) \\ _links \\ self \\ href""", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
          .bodyHasPath(s"""_embedded \\ ${sourceType.name}(1) \\ _links \\ self \\ href""", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
          .bodyHasPath(s"""_embedded \\ ${sourceType.name}(2) \\ _links \\ self \\ href""", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
      }
    }

    "return 200 and a valid response when an existing source is modified" in {
      SourceTypes.types.foreach { sourceType =>
        when()
          .put(s"/sandbox/$saUtr/$taxYear/${sourceType.name}/$sourceId", Some(sourceType.example()))
          .thenAssertThat()
          .statusIs(200)
          .contentTypeIsHalJson()
          .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId".r)
          .bodyHasSummaryLinks(sourceType, sourceId, saUtr, taxYear)
      }
    }

    "return 204 response when an existing source is deleted" in {
      SourceTypes.types.foreach { sourceType =>
        when()
          .delete(s"/sandbox/$saUtr/$taxYear/${sourceType.name}/$sourceId")
          .thenAssertThat()
          .statusIs(204)
      }
    }
  }
}
