package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.JsString
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesResourceSpec extends BaseFunctionalSpec {

  "creating a property business" should {
    "return code 201 containing a location header when creating a property business" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().properties.willBeCreatedFor(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/uk-properties".r)
    }

    "return code 409 when attempting to create the same property business more than once" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().properties.willConflict(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(409)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/uk-properties".r)
    }

    "return code 400 when attempting to create a property business with invalid information" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(JsString("OOPS")).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.invalidRequest)
    }

    "return code 400 when attempting to create a property business that fails DES validation" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().payloadFailsValidationFor(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.invalidPayload)
    }

    "return code 404 when attempting to create a property business that fails DES nino validation" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().ninoNotFoundFor(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 500 when DES is experiencing issues" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().serverErrorFor(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when systems that DES is dependant on are experiencing issues" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().serviceUnavailableFor(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().isATeapotFor(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

  }

  "retrieving a property business" should {
    "return code 200 when creating a property business exists" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().properties.willBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(200)
    }

    "return code 404 when DES does not return property business" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().properties.willReturnNone(nino)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 404 when attempting to create a property business that fails DES nino validation" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().ninoNotFoundFor(nino)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(404)
    }


    "return code 500 when DES is experiencing issues" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().serverErrorFor(nino)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when systems that DES is dependant on are experiencing issues" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().serviceUnavailableFor(nino)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .des().isATeapotFor(nino)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }
  }

}
