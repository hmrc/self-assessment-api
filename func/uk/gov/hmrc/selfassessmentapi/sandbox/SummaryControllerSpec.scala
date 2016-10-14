package uk.gov.hmrc.selfassessmentapi.sandbox

import play.api.libs.json.Json._
import play.api.test.FakeApplication
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.controllers.api.SourceTypes
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.Income
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.IncomeType._
import uk.gov.hmrc.support.BaseFunctionalSpec

class SummaryControllerSpec extends BaseFunctionalSpec {

  override lazy val app = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.self-employments.enabled" -> true,
    "Test.feature-switch.unearned-incomes.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.uk.enabled" -> true,
    "Test.feature-switch.furnished-holiday-lettings.eea.enabled" -> true,
    "Test.feature-switch.employments.enabled" -> true,
    "Test.feature-switch.banks.enabled" -> true,
    "Test.feature-switch.uk-properties.enabled" -> true))

  val sourceId = BSONObjectID.generate.stringify
  val summaryId = BSONObjectID.generate.stringify

  "Create summary with valid data" should {
    "return a 201 when the resource is created" in {
      SourceTypes.types.foreach { sourceType =>
        sourceType.summaryTypes.foreach { summaryType =>
          when()
            .post(s"/sandbox/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}", Some(summaryType.example()))
            .thenAssertThat()
            .statusIs(201)
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/.+".r)
        }
      }
    }
  }

  // Only a single validation test is needed to show the validator is invoked. All validations are tested at a unit level.
  "Create summary with invalid data" should {
    "return a 400 validation error" in {
      when()
        .post(s"/sandbox/$saUtr/$taxYear/self-employments/$sourceId/incomes", Some(toJson(Income(None, Turnover, BigDecimal(-1000.12)))))
        .thenAssertThat()
        .isValidationError("/amount", "INVALID_MONETARY_AMOUNT")
    }
  }

  "Creating summary with invalid summary type" should {
    "return a 404" in {
      when()
        .post(s"/sandbox/$saUtr/$taxYear/self-employments/$sourceId/incoms", Some(toJson(Income(None, Turnover, BigDecimal(-1000.12)))))
        .thenAssertThat()
        .isNotFound
    }
  }

  "Retrieve an existent summary id" should {
    "return a HAL resource" in {
      SourceTypes.types.foreach { sourceType =>
        sourceType.summaryTypes.foreach { summaryType =>
          when()
            .get(s"/sandbox/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/$summaryId")
            .thenAssertThat()
            .statusIs(200)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/$summaryId")
        }
      }
    }
  }

  "Retrieve all summaries" should {
    "return all summaries as HAL resources" in {
      SourceTypes.types.foreach { sourceType =>
        sourceType.summaryTypes.foreach { summaryType =>
          when()
            .get(s"/sandbox/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}")
            .thenAssertThat()
            .statusIs(200)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}")
            .bodyHasPath(s"""_embedded \\ ${summaryType.name}(0) \\ _links \\ self \\ href""", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/.+".r)
            .bodyHasPath(s"""_embedded \\ ${summaryType.name}(1) \\ _links \\ self \\ href""", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/.+".r)
            .bodyHasPath(s"""_embedded \\ ${summaryType.name}(2) \\ _links \\ self \\ href""", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/.+".r)
        }
      }
    }
  }

  "Update summary" should {
    "return a 200 with HAL resource" in {
      SourceTypes.types.foreach { sourceType =>
        sourceType.summaryTypes.foreach { summaryType =>
          when()
            .put(s"/sandbox/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/$summaryId", Some(summaryType.example()))
            .thenAssertThat()
            .statusIs(200)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/$summaryId")
        }
      }
    }
  }

  "Delete summary" should {
    "return a 204 response" in {
      SourceTypes.types.foreach { sourceType =>
        sourceType.summaryTypes.foreach { summaryType =>
          when()
            .delete(s"/sandbox/$saUtr/$taxYear/${sourceType.name}/$sourceId/${summaryType.name}/$summaryId")
            .thenAssertThat()
            .statusIs(204)
        }
      }
    }
  }

}
