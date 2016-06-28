package uk.gov.hmrc.selfassessmentapi

import java.util.UUID

import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.domain.employment.SourceType.Employments
import uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings.SourceType.FurnishedHolidayLettings
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.domain.ukproperty.SourceType.UKProperties
import uk.gov.hmrc.support.BaseFunctionalSpec

class FeatureSwitchSpec extends BaseFunctionalSpec {

  val sourceId = UUID.randomUUID().toString
  val summaryId = UUID.randomUUID().toString

  private val conf: Map[String, Map[SourceId, Map[SourceId, Map[SourceId, Any]]]] =
    Map("Test" ->
      Map("feature-switch" ->
        Map(
          "employments" -> Map("enabled" -> true),
          "self-employments" -> Map("enabled" -> false),
          "furnished-holiday-lettings" -> Map("enabled" -> false),
          "uk-properties" -> Map("enabled" -> true, "expenses" -> Map("enabled" -> false)))))

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = conf)

  object Status extends Enumeration {
    type Status = Value
    val BLOCKED, VISIBLE = Value
  }

  trait Mode {
    def url: String
  }

  case object LIVE extends Mode {
    override def url = ""
  }

  case object SANDBOX extends Mode {
    override def url = "/sandbox"
  }

  def statusCode(status: Status.Value, method: String)(implicit mode: Mode): Int = {
    mode match {
      case SANDBOX => if (method.equalsIgnoreCase("post")) 201 else 200
      case LIVE =>
        status match {
          case Status.VISIBLE => 501
          case Status.BLOCKED => 404
        }
    }
  }

  val sourceToExpenses = Map(SelfEmployments -> selfemployment.SummaryTypes.Expenses,
    FurnishedHolidayLettings -> furnishedholidaylettings.SummaryTypes.Expenses,
    Employments -> employment.SummaryTypes.Expenses)

  "self-employments and Furnished Holiday Lettings resource" should {
    "be blocked" in {
      Map(SelfEmployments -> Status.BLOCKED/*, FurnishedHolidayLettings -> Status.BLOCKED, Employments -> Status.VISIBLE*/).foreach {
        case (source, status) =>
          Seq(SANDBOX, LIVE).foreach { implicit mode =>
            given()
              .userIsAuthorisedForTheResource(saUtr)
              .when()
              .get(s"${mode.url}/$saUtr/$taxYear/${source.name}")
              .thenAssertThat()
              .statusIs(statusCode(status, "GET"))

            given()
              .userIsAuthorisedForTheResource(saUtr)
              .when()
              .get(s"${mode.url}/$saUtr/$taxYear/${source.name}/$sourceId")
              .thenAssertThat()
              .statusIs(statusCode(status, "GET"))

            given()
              .userIsAuthorisedForTheResource(saUtr)
              .when()
              .get(s"${mode.url}/$saUtr/$taxYear/${source.name}/$sourceId/expenses/$summaryId")
              .thenAssertThat()
              .statusIs(statusCode(status, "GET"))

            given()
              .userIsAuthorisedForTheResource(saUtr)
              .when()
              .post(s"${mode.url}/$saUtr/$taxYear/${source.name}/$sourceId/expenses", Some(sourceToExpenses(source).example()))
              .thenAssertThat()
              .statusIs(statusCode(status, "POST"))

            given()
              .userIsAuthorisedForTheResource(saUtr)
              .when()
              .put(s"${mode.url}/$saUtr/$taxYear/${source.name}/$sourceId/expenses/$summaryId", Some(sourceToExpenses(source).example()))
              .thenAssertThat()
              .statusIs(statusCode(status, "PUT"))

            given()
              .userIsAuthorisedForTheResource(saUtr)
              .when()
              .post(s"${mode.url}/$saUtr/$taxYear/${source.name}", Some(source.example()))
              .thenAssertThat()
              .statusIs(statusCode(status, "POST"))

            given()
              .userIsAuthorisedForTheResource(saUtr)
              .when()
              .put(s"${mode.url}/$saUtr/$taxYear/${source.name}/$sourceId", Some(source.example()))
              .thenAssertThat()
              .statusIs(statusCode(status, "PUT"))
          }
      }
    }
  }

  "only expenses resources for uk-properties" should {
    "be disabled" in {
      when()
        .get(s"/sandbox/$saUtr/$taxYear/uk-properties")
        .thenAssertThat()
        .statusIs(200)

      when()
        .get(s"/sandbox/$saUtr/$taxYear/uk-properties/$sourceId")
        .thenAssertThat()
        .statusIs(200)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/sandbox/$saUtr/$taxYear/uk-properties", Some(UKProperties.example()))
        .thenAssertThat()
        .statusIs(201)

      when()
        .get(s"/sandbox/$saUtr/$taxYear/uk-properties/$sourceId/expenses")
        .thenAssertThat()
        .statusIs(200)

      when()
        .get(s"/sandbox/$saUtr/$taxYear/uk-properties/$sourceId/expenses/$summaryId")
        .thenAssertThat()
        .statusIs(200)

      when()
        .put(s"/sandbox/$saUtr/$taxYear/uk-properties/$sourceId/expenses/$summaryId", Some(ukproperty.SummaryTypes.Expenses.example()))
        .thenAssertThat()
        .statusIs(200)
    }
  }

}
