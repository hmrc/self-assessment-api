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

class GiftAidPaymentsResourceFunctionalSpec  extends BaseFunctionalSpec {

  "update gift aids payments" should {
    "return code 204 when updating payments" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource
        .des()
        .GiftAid
        .updatePayments(nino, taxYear)
        .when()
        .put(Jsons.GiftAidPayments(100.00))
        .at(s"/ni/$nino/gift-aid/$taxYear")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 404 when updating payments with invalid nino" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource
        .des()
        .GiftAid
        .updatePaymentsWithNinoNotAvailable(nino, taxYear)
        .when()
        .put(Jsons.GiftAidPayments(100.00))
        .at(s"/ni/$nino/gift-aid/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }

    s"return code 400 when attempting to update the gift aid payments with invalid totalOneOffPayments" in {

      val expectedJson = Jsons.Errors.invalidRequest("INVALID_MONETARY_AMOUNT" -> "/totalOneOffPayments")

      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource
        .when()
        .put(Jsons.GiftAidPayments(-100.00))
        .at(s"/ni/$nino/gift-aid/$taxYear")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(expectedJson)
    }

    s"return code 400 when attempting to update the gift aid payments with invalid totalPayments" in {

      val expectedJson = Jsons.Errors.invalidRequest("TOTAL_PAYMENTS_LESS" -> "")

      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource
        .when()
        .put(Jsons.GiftAidPayments(500.00))
        .at(s"/ni/$nino/gift-aid/$taxYear")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsLike(expectedJson)
    }
  }

  "retrieving the gift aid payments" should {

    s"return code 200 with a JSON array of gift-aid payments belonging to the given nino and tax year" in {
      val expectedJson = Jsons.GiftAidPayments(100.00)
      given()
        .userIsSubscribedToMtdFor(nino)
        .userIsFullyAuthorisedForTheResource
        .des()
        .GiftAid
        .retrievePayments(nino, taxYear)
        .when()
        .get(s"/ni/$nino/gift-aid/$taxYear")
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
        .get(s"/ni/$nino/gift-aid/$taxYear")
        .thenAssertThat()
        .statusIs(404)
        .bodyHasPath("\\code", "NOT_FOUND_NINO")
    }
  }
}
