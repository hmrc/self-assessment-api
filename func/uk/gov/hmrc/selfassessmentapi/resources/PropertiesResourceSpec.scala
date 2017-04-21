package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.JsString
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesResourceSpec extends BaseFunctionalSpec {

  "creating a property business" should {
    "return code 201 containing a location header when creating a uk property business" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/uk-properties".r)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Properties().toString())
    }

    "return code 409 when attempting to create the same property business more than once" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(409)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/uk-properties".r)
    }

    "return code 400 when attempting to create a property business with invalid information" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .when()
        .post(JsString("OOPS")).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(400)
    }
  }

  "retrieving a property business" should {
    "return code 404 when accessing a property business which doesn't exists" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(404)
    }
  }




}
