/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.r2.selfassessmentapi.resources

import uk.gov.hmrc.r2.selfassessmentapi.support.BaseFunctionalSpec

class PropertiesResourceISpec extends BaseFunctionalSpec {

  "creating a property business" should {
    "return code 201 containing a location header when creating a property business" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.willBeCreatedFor(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/uk-properties".r)
        .when()
    }

    "return code 409 when attempting to create the same property business more than once" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.willConflict(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(409)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/uk-properties".r)
    }

    "return code 400 when attempting to create a property business with invalid information" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().payloadFailsValidationFor(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.invalidRequest)
    }

    "return code 400 when attempting to create a property business that fails DES nino validation" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().invalidNinoFor(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.ninoInvalid)
    }

    "return code 500 when DES is experiencing issues" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().serverErrorFor(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when systems that DES is dependant on are experiencing issues" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().serviceUnavailableFor(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
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
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.willBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(200)
    }

    "return code 404 when DES does not return property business" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.willReturnNone(nino)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when attempting to create a property business that fails DES nino validation" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().invalidNinoFor(nino)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.ninoInvalid)
    }

    "return code 500 when DES is experiencing issues" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().serverErrorFor(nino)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when systems that DES is dependant on are experiencing issues" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().serviceUnavailableFor(nino)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().isATeapotFor(nino)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(500)
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return accounting period start date" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.willBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .body(_ \ "accountingPeriod" \ "start").is("2001-01-01")
    }

    "return accounting period end date" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.willBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .body(_ \ "accountingPeriod" \ "end").is("2010-01-01")
    }
  }

}
