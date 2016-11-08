package uk.gov.hmrc.selfassessmentapi.live

import org.joda.time.LocalDate
import play.api.libs.json.JsValue
import play.api.libs.json.Json.{parse, toJson}
import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode.COMMENCEMENT_DATE_NOT_IN_THE_PAST
import uk.gov.hmrc.selfassessmentapi.controllers.api.bank.SourceType.Banks
import uk.gov.hmrc.selfassessmentapi.controllers.api.benefit.SourceType.Benefits
import uk.gov.hmrc.selfassessmentapi.controllers.api.dividend.SourceType.Dividends
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.FurnishedHolidayLetting
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.PropertyLocationType.EEA
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.SourceType.FurnishedHolidayLettings
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SelfEmployment
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SelfEmployment._
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.SourceType.UKProperties
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.UKProperty
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceType, SourceTypes}
import uk.gov.hmrc.support.BaseFunctionalSpec

import scala.util.matching.Regex

case class ExpectedError(path: String, code: String, httpStatusCode: Regex = "400".r)
case class ExpectedUpdate(path: JsValue => JsValue, value: String = "")

case class ErrorScenario(invalidInput: JsValue, error: ExpectedError)
case class UpdateScenario(updatedValue: JsValue, expectedUpdate: ExpectedUpdate)

class SourceControllerSpec extends BaseFunctionalSpec {

  override lazy val app = FakeApplication(
    additionalConfiguration = Map("Test.feature-switch.self-employments.enabled" -> true,
                                  "Test.feature-switch.benefits.enabled" -> true,
                                  "Test.feature-switch.furnished-holiday-lettings.enabled" -> true,
                                  "Test.feature-switch.furnished-holiday-lettings.uk.enabled" -> true,
                                  "Test.feature-switch.furnished-holiday-lettings.eea.enabled" -> true,
                                  "Test.feature-switch.employments.enabled" -> true,
                                  "Test.feature-switch.uk-properties.enabled" -> true,
                                  "Test.feature-switch.banks.enabled" -> true,
                                  "Test.source-limits.self-employments" -> false))

  val ok: Regex = "20.".r

  val errorScenarios: Map[SourceType, ErrorScenario] = Map(
    SelfEmployments -> ErrorScenario(
      invalidInput = toJson(SelfEmployment.example().copy(commencementDate = LocalDate.now().plusDays(1))),
      error = ExpectedError(path = "/commencementDate", code = s"$COMMENCEMENT_DATE_NOT_IN_THE_PAST")),
    Benefits -> ErrorScenario(invalidInput = toJson(Benefits.example()),
                              error = ExpectedError(path = "", code = "", httpStatusCode = ok)),
    FurnishedHolidayLettings -> ErrorScenario(invalidInput = parse(s"""
                                                                      |{
                                                                      |  "propertyLocation": "The Moon"
                                                                      |}
                                                                    """.stripMargin),
                                              error =
                                                ExpectedError(path = "/propertyLocation", code = "NO_VALUE_FOUND")),
    UKProperties -> ErrorScenario(invalidInput = parse(s"""
                                                          |{
                                                          |  "rentARoomRelief": "1000.456"
                                                          |}
                                                        """.stripMargin),
                                  error = ExpectedError(path = "/rentARoomRelief", code = "INVALID_MONETARY_AMOUNT")),
    Dividends -> ErrorScenario(invalidInput = toJson(Dividends.example()),
                               error = ExpectedError(path = "", code = "", httpStatusCode = ok)),
    Banks -> ErrorScenario(invalidInput = toJson(Banks.example()),
                           error = ExpectedError(path = "", code = "", httpStatusCode = ok))
  )

  val updateScenarios: Map[SourceType, UpdateScenario] = Map(
    SelfEmployments -> UpdateScenario(
      updatedValue = toJson(SelfEmployment.example().copy(commencementDate = LocalDate.now().minusDays(1))),
      expectedUpdate =
        ExpectedUpdate(path = _ \ "commencementDate", value = LocalDate.now().minusDays(1).toString("yyyy-MM-dd"))),
    Benefits -> UpdateScenario(updatedValue = toJson(Benefits.example()),
                               expectedUpdate = ExpectedUpdate(path = _ \ "_id", value = "")),
    FurnishedHolidayLettings -> UpdateScenario(
      updatedValue = toJson(FurnishedHolidayLetting.example().copy(propertyLocation = EEA)),
      expectedUpdate = ExpectedUpdate(path = _ \ "_id", value = "")),
    UKProperties -> UpdateScenario(updatedValue = toJson(UKProperty.example().copy(rentARoomRelief = Some(7777))),
                                   expectedUpdate = ExpectedUpdate(path = _ \ "_id", value = "")),
    Dividends -> UpdateScenario(updatedValue = toJson(Dividends.example()),
                                expectedUpdate = ExpectedUpdate(path = _ \ "_id", value = "")),
    Banks -> UpdateScenario(updatedValue = toJson(Banks.example()),
                            expectedUpdate = ExpectedUpdate(path = _ \ "_id", value = ""))
  )

  "I" should {
    "be able to create, update and delete a self assessment source" in {
      SourceTypes.types.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}")
          .withAcceptHeader()
          .thenAssertThat()
          .statusIs(200)
          .butResponseHasNo(sourceType.name)
        when()
          .post(Some(sourceType.example()))
          .to(s"/$saUtr/$taxYear/${sourceType.name}")
          .withAcceptHeader()
          .thenAssertThat()
          .statusIs(201)
          .contentTypeIsHalJson()
          .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/.+".r)
          .bodyHasSummaryLinks(sourceType, saUtr, taxYear)
          .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .withAcceptHeader()
          .thenAssertThat()
          .statusIs(200)
          .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .when()
          .put(Some(updateScenarios(sourceType).updatedValue))
          .at(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .withAcceptHeader()
          .thenAssertThat()
          .statusIs(200)
          .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .withAcceptHeader()
          .thenAssertThat()
          .statusIs(200)
          .body(updateScenarios(sourceType).expectedUpdate.path)
          .is(updateScenarios(sourceType).expectedUpdate.value)
          .when()
          .delete(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .thenAssertThat()
          .statusIs(204)
          .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%")
          .withAcceptHeader()
          .thenAssertThat()
          .isNotFound
      }
    }
  }

  "I" should {
    "not be able to get a invalid source type" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/blah")
        .withAcceptHeader()
        .thenAssertThat()
        .isNotFound
    }

    "not be able to get a non-existent source" in {
      SourceTypes.types.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .get(s"/$saUtr/$taxYear/${sourceType.name}/asdfasdf")
          .withAcceptHeader()
          .thenAssertThat()
          .isNotFound

        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .put(s"/$saUtr/$taxYear/${sourceType.name}/asdfasdf", Some(sourceType.example()))
          .withAcceptHeader()
          .thenAssertThat()
          .isNotFound

        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .delete(s"/$saUtr/$taxYear/${sourceType.name}/asdfasdf")
          .withAcceptHeader()
          .thenAssertThat()
          .isNotFound

      }
    }

    "not be able to create a source with invalid data" in {
      SourceTypes.types.filter(errorScenarios(_).error.httpStatusCode != ok).foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(errorScenarios(sourceType).invalidInput))
          .withAcceptHeader()
          .thenAssertThat()
          .isValidationError(errorScenarios(sourceType).error.path, errorScenarios(sourceType).error.code)
      }
    }

    "not be able to update a source with invalid data" in {
      SourceTypes.types.filter(errorScenarios(_).error.httpStatusCode != ok).foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .post(s"/$saUtr/$taxYear/${sourceType.name}", Some(sourceType.example()))
          .thenAssertThat()
          .statusIs(201)
          .when()
          .put(s"/$saUtr/$taxYear/${sourceType.name}/%sourceId%", Some(errorScenarios(sourceType).invalidInput))
          .thenAssertThat()
          .isValidationError(errorScenarios(sourceType).error.path, errorScenarios(sourceType).error.code)
      }
    }

    "not be able to update a non-existent" in {
      SourceTypes.types.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .put(s"/$saUtr/$taxYear/${sourceType.name}/non-existent-source",
               Some(updateScenarios(sourceType).updatedValue))
          .thenAssertThat()
          .isNotFound
      }
    }

    "not be able to delete a non-existent" in {
      SourceTypes.types.foreach { sourceType =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .delete(s"/$saUtr/$taxYear/${sourceType.name}/non-existent-source")
          .thenAssertThat()
          .isNotFound
      }
    }
  }
}
