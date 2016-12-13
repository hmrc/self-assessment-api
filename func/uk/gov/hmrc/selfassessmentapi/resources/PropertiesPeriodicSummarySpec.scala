package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.selfassessmentapi.resources.models.PeriodId
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesPeriodicSummarySpec extends BaseFunctionalSpec {

  "createPeriod" should {
    "return code 201 containing a location header when creating a uk property period" ignore {
      val period = Jsons.Properties.period(fromDate = Some("2016-04-06"), toDate = Some("2017-04-05"),
        rentIncome = 50.55, premiumsOfLeaseGrant = 20.22, reversePremiums = 100.25,
        premisesRunningCosts = (50.55, 10.12), otherCost = (10.22, 10.12))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(period).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/properties/uk/periods/\\w+".r)
    }

    "return code 400 when provided with an invalid uk property period" ignore {

      val period = Jsons.Properties.period(fromDate = Some("2016-04-06"), toDate = Some("2017-04-05"),
        rentIncome = -50.55, premiumsOfLeaseGrant = 20.22, reversePremiums = 100.25,
        premisesRunningCosts = (50.55, 10.12), otherCost = (10.22, 10.12))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(period).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_MONETARY_AMOUNT", "/incomes/rentIncome/amount")))
    }
  }

  "retrievePeriods" should {
    "return code 200 when retrieving all periods associated with a properties business" ignore {
      val periodOne = Jsons.Properties.period(fromDate = Some("2016-04-06"), toDate = Some("2016-05-05"))
      val periodTwo = Jsons.Properties.period(fromDate = Some("2016-05-06"), toDate = Some("2016-06-05"))

      val expectedBody =
        s"""
           |[
           |  {
           |    "from": "2016-04-06",
           |    "to": "2016-05-05"
           |  },
           |  {
           |    "from": "2016-05-06",
           |    "to": "2016-06-05"
           |  }
           |]
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(periodOne).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodTwo).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
        .selectFields(_ \\ "id").isLength(2).matches("\\w+".r)
    }
  }

  "retrievePeriod" should {
    "return code 200 when retrieving a period associated with a specific identifier" ignore {
      val period = Jsons.Properties.period(fromDate = Some("2016-04-06"), toDate = Some("2016-05-06"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(period).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(period.toString())
        .bodyDoesNotHavePath[PeriodId]("id")
    }

    "return code 404 when retrieving a non-existent period with a bad identifier" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/properties/uk/periods/ohno")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "updatePeriod" should {
    "return code 204 when updating a period associated with a specific identifier" ignore {
      val period = Jsons.Properties.period(fromDate = Some("2016-04-06"), toDate = Some("2016-05-06"))

      val periodicUpdate = Jsons.Properties.period(fromDate = None, toDate = None,
        rentIncome = 50.55, premisesRunningCosts = (50.55, 10.12))

      val updatedPeriod = Jsons.Properties.period(fromDate = Some("2016-04-06"), toDate = Some("2016-05-06"),
        rentIncome = 50.55, premisesRunningCosts = (50.55, 10.12))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(period).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(periodicUpdate).at(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get("%periodLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(updatedPeriod.toString())
    }

    "return code 400 when provided with an invalid period" ignore {
      val period = Jsons.Properties.period(fromDate = Some("2016-04-06"), toDate = Some("2016-05-06"))

      val periodicUpdate = Jsons.Properties.period(fromDate = None, toDate = None,
        rentIncome = -50.55, premisesRunningCosts = (50.55, 10.12))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(period).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(periodicUpdate).at(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_MONETARY_AMOUNT", "/incomes/rentIncome/amount")))
    }

    "return code 404 when attempting to update a non-existent period" in {

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .put(Jsons.Properties.period(rentIncome = 50.55)).at(s"/ni/$nino/properties/uk/periods/ohno")
        .thenAssertThat()
        .statusIs(404)
    }
  }
}
