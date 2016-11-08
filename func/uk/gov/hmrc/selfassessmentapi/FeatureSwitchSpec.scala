package uk.gov.hmrc.selfassessmentapi

import java.util.UUID

import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.controllers.api.SourceId
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.support.BaseFunctionalSpec

class FeatureSwitchSpec extends BaseFunctionalSpec {

  val sourceId = UUID.randomUUID().toString
  val summaryId = UUID.randomUUID().toString

  private val conf: Map[String, Map[SourceId, Map[SourceId, Map[SourceId, Any]]]] =
    Map("Test" ->
      Map("feature-switch" ->
        Map(
          "self-employments" -> Map("enabled" -> true, "incomes" -> Map("enabled" -> true),
            "expenses" -> Map("enabled" -> false), "balancing-charges" -> Map("enabled" -> false),
            "goods-and-services-own-uses" -> Map("enabled" -> false)))
          ))

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = conf)

  "self-employments and only self-employment incomes" should {
    "be visible in live" in {

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/${SelfEmployments.name}")
        .thenAssertThat()
        .statusIs(200)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/${SelfEmployments.name}/$sourceId/incomes")
        .thenAssertThat()
        .statusIs(200)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/${SelfEmployments.name}/$sourceId/expenses")
        .thenAssertThat()
        .isNotImplemented

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear/${SelfEmployments.name}/$sourceId/balancing-charges")
        .thenAssertThat()
        .isNotImplemented
    }

    "be visible in sandbox" in {

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/sandbox/$saUtr/$taxYear/${SelfEmployments.name}")
        .thenAssertThat()
        .statusIs(200)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/sandbox/$saUtr/$taxYear/${SelfEmployments.name}/$sourceId/incomes")
        .thenAssertThat()
        .statusIs(200)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/sandbox/$saUtr/$taxYear/${SelfEmployments.name}/$sourceId/expenses")
        .thenAssertThat()
        .isNotImplemented

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/sandbox/$saUtr/$taxYear/${SelfEmployments.name}/$sourceId/balancing-charges")
        .thenAssertThat()
        .isNotImplemented
    }
  }

}
