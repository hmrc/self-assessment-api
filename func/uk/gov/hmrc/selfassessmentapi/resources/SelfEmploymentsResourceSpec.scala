package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.models.SourceId
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentsResourceSpec extends BaseFunctionalSpec {

  "create" should {
    "return code 201 containing a location header when creating a valid a self-employment source of income" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().selfEmployment.willBeCreatedFor(nino)
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

    "return code 400 when attempting to create a self-employment that fails DES validation" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().payloadFailsValidationFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.invalidPayload)
    }

    "return code 404 when attempting to create a self-employment that fails DES nino validation" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().ninoNotFoundFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when attempting to create a self-employment that fails DES duplicated trading name validaton" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().selfEmployment.failsTradingName(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.duplicateTradingName)
    }

    "return code 500 when DES is experiencing issues" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().serverErrorFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when systems that DES is dependant on are experiencing issues" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().serviceUnavailableFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 403 Unauthorized when attempting to create more than one self-employment source" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().selfEmployment.tooManySourcesFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(403)
        .bodyIsLike(Jsons.Errors.businessError("TOO_MANY_SOURCES" -> ""))
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().isATeapotFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(500)
    }
  }

  "update" should {
    "return code 204 when successfully updating a self-employment resource" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().selfEmployment.willBeCreatedFor(nino)
        .des().selfEmployment.willBeUpdatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.SelfEmployment.update()).at("%sourceLocation%")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 404 when attempting to update a non-existent self-employment resource" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().selfEmployment.willNotBeUpdatedFor(nino)
        .when()
        .put(Jsons.SelfEmployment.update()).at(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 (MANDATORY_FIELD_MISSING) when attempting to update a self-employment with an empty body" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().selfEmployment.willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Json.parse("{}")).at(s"%sourceLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(
          ("MANDATORY_FIELD_MISSING", "/tradingName"),
          ("MANDATORY_FIELD_MISSING", "/businessDescription"),
          ("MANDATORY_FIELD_MISSING", "/businessAddressLineOne"),
          ("MANDATORY_FIELD_MISSING", "/businessPostcode")))
    }

    "return code 400 (INVALID_BUSINESS_DESCRIPTION) when attempting to update a self-employment with an invalid business description" in {
      val updatedSelfEmployment = Jsons.SelfEmployment.update(businessDescription = "invalid")

      given()
        .userIsAuthorisedForTheResource(nino)
        .des().selfEmployment.willBeCreatedFor(nino)
        .when()
        .post(Jsons.SelfEmployment()).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(updatedSelfEmployment).at(s"%sourceLocation%")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest(
          ("INVALID_BUSINESS_DESCRIPTION", "/businessDescription")))
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().isATeapotFor(nino)
        .when()
        .put(Jsons.SelfEmployment.update()).at(s"/ni/$nino/self-employments/abc")
        .thenAssertThat()
        .statusIs(500)
    }
  }

  "retrieve" should {
    "return code 200 when retrieving a self-employment resource that exists" in {
      val expectedSelfEmployment = Jsons.SelfEmployment(cessationDate = None, businessDescription = None)

      given()
        .userIsAuthorisedForTheResource(nino)
        .des().selfEmployment.willBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedSelfEmployment.toString())
        .bodyDoesNotHavePath[SourceId]("id")
    }

    "return code 400 when attempting to retrieve a self-employment that fails DES nino validation" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().invalidNinoFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/sourceId")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.invalidNino)
    }

    "return code 404 when retrieving a self-employment resource that does not exist" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().ninoNotFoundFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when retrieving a self-employment that fails nino validation" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/teapot/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.ninoInvalid)
    }

    "return code 404 when retrieving a self-employment for a nino that does not exist" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().ninoNotFoundFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 500 when DES is experiencing issues" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().serverErrorFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when systems that DES is dependant on are experiencing issues" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().serviceUnavailableFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/invalidSourceId")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().isATeapotFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc")
        .thenAssertThat()
        .statusIs(500)
    }
  }

  "retrieveAll" should {
    "return code 200 when retrieving self-employments that exist" in {

      val expectedBody =
        s"""
           |[
           |  ${Jsons.SelfEmployment(cessationDate = None, businessDescription = None).toString()}
           |]
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .des().selfEmployment.willBeReturnedFor(nino)
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
        .des().selfEmployment.noneFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(200)
        .jsonBodyIsEmptyArray()
    }

    "return code 400 when attempting to retrieve self-employments that fails DES nino validation" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().invalidNinoFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.invalidNino)
    }

    "return code 404 when attempting to retrieve self-employments that do not exist" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().ninoNotFoundFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().isATeapotFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(500)
    }
  }

}
