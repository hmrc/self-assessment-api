package uk.gov.hmrc.r2.selfassessmentapi

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.r2.selfassessmentapi.models.Errors
import uk.gov.hmrc.r2.selfassessmentapi.resources.Jsons
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfAssessmentEndOfPeriodObligationsSpec extends BaseFunctionalSpec {

  "Retrieving end-of-period statement obligations" should {

    val from = new LocalDate(2017, 1, 1)
    val to = new LocalDate(2017, 12, 31)

    val testRefNo = "abc"
    val validSelfEmploymentId = "AABB12345678912"

    "return code 200 with a set of obligations" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.returnEndOfPeriodObligationsFor(nino, validSelfEmploymentId)
        .when()
        .get(s"/ni/$nino/self-employments/$validSelfEmploymentId/end-of-period-statements/obligations?from=$from&to=$to")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations.eops.toString)
    }

    "return code 400 when self-employment-id is invalid" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.returnEndOfPeriodObligationsFor(nino, testRefNo)
        .when()
        .get(s"/ni/$nino/self-employments/$testRefNo/end-of-period-statements/obligations?from=$from&to=$to")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsError("SELF_EMPLOYMENT_ID_INVALID")
    }

    "return code 404 when obligations with no 'identification' data is returned" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.returnEopsObligationsWithNoIdentificationFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/$validSelfEmploymentId/end-of-period-statements/obligations?from=$from&to=$to")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when from date is invalid" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.returnEndOfPeriodObligationsFor(nino, validSelfEmploymentId)
        .when()
        .get(s"/ni/$nino/self-employments/$validSelfEmploymentId/end-of-period-statements/obligations?from=ABC&to=$to")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Json.toJson(Errors.InvalidDate).toString)
    }


    "return code 400 when to date is invalid" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.returnEndOfPeriodObligationsFor(nino, validSelfEmploymentId)
        .when()
        .get(s"/ni/$nino/self-employments/$validSelfEmploymentId/end-of-period-statements/obligations?from=$from&to=ABC")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Json.toJson(Errors.InvalidDate).toString)
    }


    "return code 400 when from and to date range is invalid" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.returnEndOfPeriodObligationsFor(nino, validSelfEmploymentId)
        .when()
        .get(s"/ni/$nino/self-employments/$validSelfEmploymentId/end-of-period-statements/obligations?from=$from&to=2016-12-31")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Json.toJson(Errors.InvalidDateRange_2).toString)
    }

    "return status 404 when and INVALID_BPKEY error is received" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.returnEopsObligationsErrorFor(nino, validSelfEmploymentId)(404, "INVALID_BPKEY")
        .when()
        .get(s"/ni/$nino/self-employments/$validSelfEmploymentId/end-of-period-statements/obligations?from=$from&to=$to")
        .thenAssertThat()
        .statusIs(404)
    }

    def testErrorScenario(desStatus:Int, desCode:String)(expectedStatus: Int, expectedError: Errors.Error): Unit = {

      s"return status $expectedStatus when a $desCode error is received" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().obligations.returnEopsObligationsErrorFor(nino, validSelfEmploymentId)(desStatus, desCode)
          .when()
          .get(s"/ni/$nino/self-employments/$validSelfEmploymentId/end-of-period-statements/obligations?from=$from&to=$to")
          .thenAssertThat()
          .statusIs(expectedStatus)
          .bodyIsError(expectedError.code)
      }
    }

    testErrorScenario(400, "INVALID_STATUS")(500, Errors.InternalServerError)
    testErrorScenario(400, "INVALID_REGIME")(500, Errors.InternalServerError)
    testErrorScenario(400, "INVALID_IDTYPE")(500, Errors.InternalServerError)
    testErrorScenario(400, "INVALID_DATE_TO")(400, Errors.InvalidDate)
    testErrorScenario(400, "INVALID_DATE_FROM")(400, Errors.InvalidDate)
    testErrorScenario(400, "INVALID_DATE_RANGE")(400, Errors.InvalidDateRange_2)
    testErrorScenario(400, "INVALID_IDNUMBER")(400, Errors.NinoInvalid)
  }

}
