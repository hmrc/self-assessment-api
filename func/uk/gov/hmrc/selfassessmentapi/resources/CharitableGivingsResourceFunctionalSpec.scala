/*
 * Copyright 2018 HM Revenue & Customs
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

class CharitableGivingsResourceFunctionalSpec  extends BaseFunctionalSpec {

  "update charitable givings" should {
    "return code 204 when updating payments" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource
        .des()
        .GiftAid
        .updatePayments(nino, taxYear)
        .when()
        .put(Jsons.CharitableGivings(100.23, 100.27))
        .at(s"/ni/$nino/charitable-giving/$taxYear")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 404 when updating charitable givings with invalid nino" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource
        .des()
        .GiftAid
        .updatePaymentsWithNinoNotAvailable(nino, taxYear)
        .when()
        .put(Jsons.CharitableGivings(100.23, 100.27))
        .at(s"/ni/$nino/charitable-giving/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }

    s"return code 400 when attempting to update the charitable givings with invalid oneOffCurrentYear" in {

      val expectedJson = Jsons.Errors.invalidRequest("INVALID_MONETARY_AMOUNT" -> "/giftAidPayments/oneOffCurrentYear")

      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource
        .when()
        .put(Jsons.CharitableGivings(-100.00, 100.00))
        .at(s"/ni/$nino/charitable-giving/$taxYear")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(expectedJson)
    }

  }

  "retrieving the charitable givings" should {

    s"return code 200 with a JSON array of payments belonging to the given nino and tax year" in {
      val expectedJson = Jsons.CharitableGivings(100.23, 100.27)
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource
        .des()
        .GiftAid
        .retrievePayments(nino, taxYear)
        .when()
        .get(s"/ni/$nino/charitable-giving/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson.toString)
    }

    s"return code 404 with nino not found error for a invalid nino and tax year" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource
        .des()
        .GiftAid
        .retrievePaymentsWithInvalidNino(nino, taxYear)
        .when()
        .get(s"/ni/$nino/charitable-giving/$taxYear")
        .thenAssertThat()
        .statusIs(404)
        .bodyHasPath("\\code", "NOT_FOUND_NINO")
    }
  }
}
