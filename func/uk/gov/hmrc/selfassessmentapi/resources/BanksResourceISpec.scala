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

class BanksResourceISpec extends BaseFunctionalSpec {

  "creating a bank interest source" should {

    "return code 201 containing a location header when creating a bank interest source" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(Jsons.Banks()).to(s"/ni/$nino/savings-accounts")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/self-assessment/ni/$nino/savings-accounts/.*".r)
        .when()
        .get("%sourceLocation%")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Banks().toString())
    }

    "return code 400 when attempting to create a bank interest source with invalid information" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(Jsons.Banks("A waaaayyyyyyyyyyyyyyyyyyyyyy to looooong account name")).to(s"/ni/$nino/savings-accounts")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.invalidRequest("INVALID_FIELD_LENGTH" -> "/accountName"))
    }
  }

  "amending a bank interest source" should {

    "return code 204 when updating bank interest source information" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(Jsons.Banks()).to(s"/ni/$nino/savings-accounts")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.Banks("Amended account name")).at("%sourceLocation%")
        .thenAssertThat()
        .statusIs(204)
        .when()
        .get("%sourceLocation%")
        .thenAssertThat()
        .statusIs(200)
        .bodyIsLike(Jsons.Banks("Amended account name").toString())
    }

    "return code 400 when updating a bank interest source with invalid information" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .post(Jsons.Banks()).to(s"/ni/$nino/savings-accounts")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Jsons.Banks("A waaaayyyyyyyyyyyyyyyyyyyyyy to looooong account name")).at("%sourceLocation%")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(Jsons.Errors.invalidRequest("INVALID_FIELD_LENGTH" -> "/accountName"))
    }

    "return code 404 when updating a a bank interest source that does not exist" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .put(Jsons.Banks()).at(s"/ni/$nino/savings-accounts/2435234523")
        .thenAssertThat()
        .statusIs(404)
    }
  }

  "retrieving a bank interest source" should {
    "return code 404 when accessing a a bank interest source which doesn't exists" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/savings-accounts/23452345235")
        .thenAssertThat()
        .statusIs(404)
    }
  }

}
