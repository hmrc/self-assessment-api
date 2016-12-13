package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.resources.models.PeriodId
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentPeriodicSummarySpec extends BaseFunctionalSpec {

  "createPeriod" should {
    "return code 201 containing a location header when creating a period" in {

      val period = Jsons.SelfEmployment.period(
        fromDate = Some("2017-04-06"),
        toDate = Some("2017-07-04"))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/self-employments/\\w+/periods/\\w+".r)
    }

    "return code 400 when attempting to create a period with the 'from' and 'to' dates are in the incorrect order" in {
      val periodOne = s"""{
                      |  "from": "2017-04-01",
                      |  "to": "2017-03-31"
                      |}""".stripMargin

      val expectedBody = Jsons.Errors.invalidRequest(("INVALID_PERIOD", ""))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(Json.parse(periodOne)).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }

    "return code 403 when attempting to create a period whose date range overlaps" in {
      val periodOne = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))
      val periodTwo = Jsons.SelfEmployment.period(fromDate = Some("2017-07-05"), toDate = Some("2017-08-04"))
      val overlappingPeriod = Jsons.SelfEmployment.period(fromDate = Some("2017-08-04"), toDate = Some("2017-09-04"))

      val expectedBody = Jsons.Errors.businessError(("OVERLAPPING_PERIOD", ""))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodOne).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodTwo).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(overlappingPeriod).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsLike(expectedBody)

    }

    "return code 403 when attempting to create a period that would leave a gap between the latest period and the one provided" in {
      val periodOne = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))
      val periodTwoWithGap = Jsons.SelfEmployment.period(fromDate = Some("2017-07-06"), toDate = Some("2017-08-04"))

      val expectedBody = Jsons.Errors.businessError(("GAP_PERIOD", ""))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodOne).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodTwoWithGap).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsLike(expectedBody)
    }
  }

  "updatePeriod" should {
    "return code 204 when updating a period that exists" in {
      val period = Jsons.SelfEmployment.period(
        fromDate = Some("2017-04-06"),
        toDate = Some("2017-07-04"),
        turnover = 100.25,
        otherIncome = 100.25,
        costOfGoodsBought = (100.25, 50.25),
        cisPaymentsToSubcontractors = (100.25, 50.25))

      val updatePeriod = Jsons.SelfEmployment.period(
        turnover = 200.25,
        otherIncome = 100.25,
        costOfGoodsBought = (200.25, 50.25),
        cisPaymentsToSubcontractors = (100.25, 55.25))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(updatePeriod).at(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get(s"%periodLocation%")
        .thenAssertThat()
        .bodyIsLike(updatePeriod.toString)
    }

    "return code 404 when attempting to update a non-existent period" in {
      val period = Jsons.SelfEmployment.period(
        fromDate = Some("2017-04-06"),
        toDate = Some("2017-07-04"),
        turnover = 100.25,
        otherIncome = 100.25,
        costOfGoodsBought = (100.25, 50.25),
        cisPaymentsToSubcontractors = (100.25, 50.25))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Json.toJson(period)).at(s"%sourceLocation%/periods/thereisnoperiodhere")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "retrievePeriod" should {
    "return code 200 when retrieving a period that exists" in {
      val period = Jsons.SelfEmployment.period(
        fromDate = Some("2017-04-06"),
        toDate = Some("2017-07-04"),
        turnover = 100.25,
        otherIncome = 100.25,
        costOfGoodsBought = (100.25, 50.25),
        cisPaymentsToSubcontractors = (100.25, 50.25))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(period).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%periodLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(period.toString)
        .bodyDoesNotHavePath[PeriodId]("id")
    }

    "return code 404 when retrieving a period that does not exist" in {

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/periods/oops")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "retrieveAllPeriods" should {
    "return code 200 when retrieving all periods where periods.size > 0, sorted by from date" in {
      val periodOne = Jsons.SelfEmployment.period(fromDate = Some("2017-04-06"), toDate = Some("2017-07-04"))
      val periodTwo = Jsons.SelfEmployment.period(fromDate = Some("2017-07-05"), toDate = Some("2017-08-04"))

      val expectedBody =
        s"""
           |[
           |  {
           |    "from": "2017-04-06",
           |    "to": "2017-07-04"
           |  },
           |  {
           |    "from": "2017-07-05",
           |    "to": "2017-08-04"
           |  }
           |]
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodOne).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodTwo).to(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
        .selectFields(_ \\ "id").isLength(2).matches("\\w+".r)
    }

    "return code 200 containing an empty json body when retrieving all periods where periods.size == 0" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%/periods")
        .thenAssertThat()
        .statusIs(200)
        .jsonBodyIsEmptyArray
    }
  }


}
