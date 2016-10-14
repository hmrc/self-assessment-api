package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json
import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.controllers.api.SourceTypes._
import uk.gov.hmrc.selfassessmentapi.controllers.api.bank.SourceType.Banks
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.SourceType.FurnishedHolidayLettings
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SummaryTypes.GoodsAndServicesOwnUses
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.SourceType.UKProperties
import uk.gov.hmrc.selfassessmentapi.controllers.api.unearnedincome.SourceType.UnearnedIncomes
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceType, SourceTypes, SummaryType}
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment
import uk.gov.hmrc.support.BaseFunctionalSpec

class SummaryControllerSpec extends BaseFunctionalSpec {

  override lazy val app = FakeApplication(
    additionalConfiguration = Map("Test.feature-switch.self-employments.enabled" -> true,
                                  "Test.feature-switch.unearned-incomes.enabled" -> true,
                                  "Test.feature-switch.furnished-holiday-lettings.enabled" -> true,
                                  "Test.feature-switch.furnished-holiday-lettings.uk.enabled" -> true,
                                  "Test.feature-switch.furnished-holiday-lettings.eea.enabled" -> true,
                                  "Test.feature-switch.employments.enabled" -> true,
                                  "Test.feature-switch.uk-properties.enabled" -> true,
                                  "Test.feature-switch.banks.enabled" -> true,
                                  "Test.source-limits.self-employments" -> false))

  private def exampleSummaryTypeValue(summaryType: SummaryType): String = {
    (summaryType.example() \ "type").asOpt[String] match {
      case Some(typeValue) => typeValue
      case _ => ""
    }
  }

  val invalidAmountTestData: Set[SummaryType] = Set(GoodsAndServicesOwnUses) ++
      FurnishedHolidayLettings.summaryTypes ++ Employments.summaryTypes ++
      UKProperties.summaryTypes

  private def invalidRequestBody(summaryType: SummaryType) = {
    if (invalidAmountTestData.contains(summaryType)) {
      Some(Json.parse(s"""{"amount":1000.123}"""))
    } else {
      Some(Json.parse(s"""{"type":"InvalidType", "amount":1000.00, "taxDeduction":1000.00}"""))
    }
  }

  private def invalidErrorResponse(summaryType: SummaryType): (String, String) = {
    if (invalidAmountTestData.contains(summaryType)) {
      ("/amount", "INVALID_MONETARY_AMOUNT")
    } else {
      ("/type", "NO_VALUE_FOUND")
    }
  }

  private val implementedSummaries = Map[SourceType, Set[SummaryType]](
    Employments -> Employments.summaryTypes,
    SelfEmployments -> SelfEmployments.summaryTypes,
    UnearnedIncomes -> UnearnedIncomes.summaryTypes,
    FurnishedHolidayLettings -> FurnishedHolidayLettings.summaryTypes,
    UKProperties -> UKProperties.summaryTypes,
    Dividends -> Dividends.summaryTypes,
    Banks -> Banks.summaryTypes)

  "I" should {
    "be able to create, get, update and delete all summaries for all sources" in {
      types foreach { sourceType =>
        implementedSummaries(sourceType) foreach { summaryType =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .get(s"/$saUtr/$taxYear/${sourceType.name}")
            .thenAssertThat()
            .statusIs(200)
            .butResponseHasNo(sourceType.name)
            .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
            .thenAssertThat()
            .statusIs(201)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
            .when()
            .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
            .thenAssertThat()
            .statusIs(200)
            .when()
            .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}")
            .thenAssertThat()
            .statusIs(200)
            .butResponseHasNo(sourceType.name, summaryType.name)
            .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}", Some(summaryType.example()))
            .thenAssertThat()
            .statusIs(201)
            .contentTypeIsHalJson()
            .bodyHasLink(
              "self",
              s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%")
            .when()
            .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%")
            .thenAssertThat()
            .statusIs(200)
            .body(_ \ "type")
            .is(exampleSummaryTypeValue(summaryType))
            .body(_ \ "amount")
            .is((summaryType.example() \ "amount").as[BigDecimal])
            .when()
            .put(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%", Some(Json.parse({
              val maybeDisallowableAmount = summaryType match {
                case selfemployment.SummaryTypes.Expenses => """"disallowableAmount": 0, """
                case _ => ""
              }
              s"""{"type":"${exampleSummaryTypeValue(summaryType)}", "amount":1200.00, $maybeDisallowableAmount "taxDeduction":1000.00}"""
            })))
            .thenAssertThat()
            .statusIs(200)
            .contentTypeIsHalJson()
            .bodyHasLink(
              "self",
              s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%")
            .when()
            .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%")
            .thenAssertThat()
            .statusIs(200)
            .body(_ \ "type")
            .is(exampleSummaryTypeValue(summaryType))
            .body(_ \ "amount")
            .is(1200.00)
            .when()
            .delete(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%")
            .thenAssertThat()
            .statusIs(204)
            .when()
            .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%")
            .withAcceptHeader()
            .thenAssertThat()
            .isNotFound
        }
      }
    }
  }

  "I" should {
    "not be able to create summary with invalid payload" in {
      types foreach { sourceType =>
        implementedSummaries(sourceType) foreach { summaryType =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
            .thenAssertThat()
            .statusIs(201)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
            .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}",
                  invalidRequestBody(summaryType))
            .withAcceptHeader()
            .thenAssertThat()
            .isValidationError(invalidErrorResponse(summaryType))
        }
      }
    }
  }

  "I" should {
    "not be able to create summary for a non existent source" in {
      val summaryTypes = uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SummaryTypes

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/$taxYear/${SourceTypes.SelfEmployments.name}/1234567890/${summaryTypes.Incomes.name}",
              Some(summaryTypes.Incomes.example()))
        .withAcceptHeader()
        .thenAssertThat()
        .isNotFound
    }
  }

  "I" should {
    "not be able to update summary with invalid payload" in {
      types foreach { sourceType =>
        implementedSummaries(sourceType) foreach { summaryType =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
            .thenAssertThat()
            .statusIs(201)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
            .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}", Some(summaryType.example()))
            .thenAssertThat()
            .statusIs(201)
            .contentTypeIsHalJson()
            .bodyHasLink(
              "self",
              s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%")
            .when()
            .put(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/%summaryId%",
                 invalidRequestBody(summaryType))
            .withAcceptHeader()
            .thenAssertThat()
            .isValidationError(invalidErrorResponse(summaryType))
        }
      }
    }
  }

  "I" should {
    "not be able to get a non existent summary" in {
      types foreach { sourceType =>
        implementedSummaries(sourceType) foreach { summaryType =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
            .thenAssertThat()
            .statusIs(201)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
            .when()
            .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/12334567")
            .withAcceptHeader()
            .thenAssertThat()
            .isNotFound
        }
      }
    }
  }

  "I" should {
    "not be able to delete a non existent summary" in {
      types foreach { sourceType =>
        implementedSummaries(sourceType) foreach { summaryType =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
            .thenAssertThat()
            .statusIs(201)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
            .when()
            .delete(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/12334567")
            .withAcceptHeader()
            .thenAssertThat()
            .isNotFound
        }
      }
    }
  }

  "I" should {
    "not be able to update a non existent summary" in {
      types foreach { sourceType =>
        implementedSummaries(sourceType) foreach { summaryType =>
          given()
            .userIsAuthorisedForTheResource(saUtr)
            .when()
            .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
            .thenAssertThat()
            .statusIs(201)
            .contentTypeIsHalJson()
            .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
            .when()
            .put(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%/${summaryType.name}/12334567",
                 Some(summaryType.example()))
            .withAcceptHeader()
            .thenAssertThat()
            .isNotFound
        }
      }
    }
  }

}
