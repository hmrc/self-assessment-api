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

package uk.gov.hmrc.selfassessmentapi.resources

import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesObligationsResourceISpec extends BaseFunctionalSpec {
  "retrieveObligations" should {
    "return code 200 containing a set of canned obligations" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().properties.willBeCreatedFor(nino)
        .des().obligations.returnObligationsFor(nino)
        .when()
        .post(Jsons.Properties()).to(s"/ni/$nino/uk-properties")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/ni/$nino/uk-properties/obligations")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations().toString)
    }

    "return code 404 when attempting to retrieve obligations for a properties business that does not exist" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/uk-properties/obligations")
        .thenAssertThat()
        .statusIs(404)
    }

    "forward the GovTestScenario header to DES" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.receivesObligationsTestHeader(nino, "ALL_MET")
        .when()
        .get(s"/ni/$nino/uk-properties/obligations").withHeaders(GovTestScenarioHeader, "ALL_MET")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Obligations().toString)
    }

    "return code 404 when obligations with no 'identification' data is returned" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.returnObligationsWithNoIdentificationFor(nino)
        .when()
        .get(s"/ni/$nino/uk-properties/obligations")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 404 when property id does not exist" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().obligations.obligationNotFoundFor(nino)
        .when()
        .get(s"/ni/$nino/uk-properties/obligations")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when nino is invalid" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().invalidNinoFor(nino)
        .when()
        .get(s"/ni/abc1234/uk-properties/obligations")
        .thenAssertThat()
        .statusIs(400)
    }
  }
}
