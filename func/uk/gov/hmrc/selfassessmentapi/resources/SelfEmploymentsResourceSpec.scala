package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.resources.models.{PeriodId, SourceId}
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsResourceSpec extends BaseFunctionalSpec {

  "create" should {
    "return code 201 containing a location header when creating a valid a self-employment source of income" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/self-employments/\\w+".r)
    }

    "return code 400 (INVALID_REQUEST) when attempting to create a self-employment with an invalid dates in the accountingPeriod" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment(accPeriodStart = "01-01-2017", accPeriodEnd = "02-01-2017")).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_DATE", "/accountingPeriod/start"), ("INVALID_DATE", "/accountingPeriod/end")))
    }

    "return code 400 (INVALID_VALUE) when attempting to create a self-employment with an invalid accounting type" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment(accountingType = "INVALID_ACC_TYPE")).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_VALUE", "/accountingType")))
    }
  }

  "update" should {
    "return code 204 when successfully updating a self-employment resource" ignore {
      val updatedSelfEmployment = Jsons.SelfEmployment(accPeriodStart = "2017-04-01", accPeriodEnd = "2017-06-01",
        accountingType = "ACCRUAL", commencementDate = "2016-01-01")

      val expectedSelfEmployment = Jsons.SelfEmployment(accPeriodStart = "2017-04-01", accPeriodEnd = "2017-06-01",
        accountingType = "ACCRUAL", commencementDate = "2016-01-01", businessDescription = "Boxes made of corrugated cardboard ")

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(updatedSelfEmployment).at("%sourceLocation%")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get("%sourceLocation%")
        .thenAssertThat()
        .bodyIsLike(expectedSelfEmployment.toString)
    }

    "return code 404 when attempting to update a non-existent self-employment resource" ignore {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.SelfEmployment()).at(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 (INVALID_DATE) when attempting to update a self-employment with a non-ISO (i.e. YYYY-MM-DD) date" ignore {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.SelfEmployment(commencementDate = "22-10-2016")).at(s"%sourceLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_DATE", "/commencementDate")))
    }

    "return code 400 (INVALID_DATE) when attempting to update a self-employment with an empty date" ignore {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.SelfEmployment(commencementDate = "")).at(s"%sourceLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_DATE", "/commencementDate")))
    }

    "return code 400 (MANDATORY_FIELD_MISSING) when attempting to update a self-employment with an empty body" ignore {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Json.parse("{}")).at(s"%sourceLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("MANDATORY_FIELD_MISSING", "/accountingPeriod"),
          ("MANDATORY_FIELD_MISSING", "/accountingType"), ("MANDATORY_FIELD_MISSING", "/commencementDate")))
    }

    "return code 400 (INVALID_VALUE) when attempting to update a self-employment with an invalid accounting type" ignore {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.SelfEmployment(accountingType = "INVALID")).at(s"%sourceLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(("INVALID_VALUE", "/accountingType")))
    }
  }

  "retrieve" should {
    "return code 200 when retrieving a self-employment resource that exists" in {
      val expectedSelfEmployment = Jsons.SelfEmployment(
        businessDescription = "Boxes made of corrugated cardboard ")

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"%sourceLocation%")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedSelfEmployment.toString())
        .bodyDoesNotHavePath[SourceId]("id")
    }

    "return code 404 when retrieving a self-employment resource that does not exist" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "retrieveAll" should {
    "return code 200 when retrieving self-employments that exist" in {

      val expectedBody =
        s"""
           |[
           |  ${Jsons.SelfEmployment(businessDescription = "Boxes made of corrugated cardboard ").toString()}
           |]
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
        .selectFields(_ \\ "id").isLength(1).matches("\\w+".r)
    }

    "return code 200 with an empty body when the user has no self-employment sources" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
        .jsonBodyIsEmptyArray
    }
  }

}
