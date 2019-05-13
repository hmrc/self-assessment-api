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

class SelfEmploymentAnnualSummaryResourceISpec extends BaseFunctionalSpec {

  "updateAnnualSummary" should {
    "return code 204 when updating an annual summary for a valid self-employment source" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.annualSummaryWillBeUpdatedFor(nino)
        .when()
        .put(Jsons.SelfEmployment.annualSummary()).at(s"/ni/$nino/self-employments/abc/$taxYear")
        .thenAssertThat()
        .statusIs(204)
    }

    "return code 404 when updating an annual summary for an invalid self-employment source" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.annualSummaryNotFoundFor(nino)
        .when()
        .put(Jsons.SelfEmployment.annualSummary()).at(s"/ni/$nino/self-employments/sillysource/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when updating an annual summary providing an invalid adjustment & allowance" in {
      val annualSummaries = Jsons.SelfEmployment.annualSummary(
        includedNonTaxableProfits = -100, overlapReliefUsed = -100,
        goodsAndServicesOwnUse = -100, capitalAllowanceMainPool = -100)

      val expectedBody = Jsons.Errors.invalidRequest(
        ("INVALID_MONETARY_AMOUNT", "/adjustments/includedNonTaxableProfits"),
        ("INVALID_MONETARY_AMOUNT", "/adjustments/overlapReliefUsed"),
        ("INVALID_MONETARY_AMOUNT", "/adjustments/goodsAndServicesOwnUse"),
        ("INVALID_MONETARY_AMOUNT", "/allowances/capitalAllowanceMainPool"))

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .put(annualSummaries).at(s"/ni/$nino/self-employments/abc/$taxYear")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
    }

    "return code 500 when provided with an invalid Originator-Id header" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.invalidOriginatorIdFor(nino)
        .when()
        .put(Jsons.SelfEmployment.annualSummary()).at(s"/ni/$nino/self-employments/abc/$taxYear")
        .thenAssertThat()
        .statusIs(500)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 400 when provided with an invalid payload" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().payloadFailsValidationFor(nino)
        .when()
        .put(Jsons.SelfEmployment.annualSummary()).at(s"/ni/$nino/self-employments/abc/$taxYear")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest)
    }

    "return code 500 when DES is experiencing problems" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().serverErrorFor(nino)
        .when()
        .put(Jsons.SelfEmployment.annualSummary()).at(s"/ni/$nino/self-employments/abc/$taxYear")
        .thenAssertThat()
        .statusIs(500)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when a dependent system is not responding" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().serviceUnavailableFor(nino)
        .when()
        .put(Jsons.SelfEmployment.annualSummary()).at(s"/ni/$nino/self-employments/abc/$taxYear")
        .thenAssertThat()
        .statusIs(500)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.internalServerError)
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().isATeapotFor(nino)
        .when()
        .put(Jsons.SelfEmployment.update()).at(s"/ni/$nino/self-employments/abc/$taxYear")
        .thenAssertThat()
        .statusIs(500)
    }
  }

  "retrieveAnnualSummary" should {
    "return code 200 when retrieving an annual summary that exists" in {
      val expectedJson = Jsons.SelfEmployment.annualSummary(
        annualInvestmentAllowance = 200,
        businessPremisesRenovationAllowance = 200,
        capitalAllowanceMainPool = 200,
        capitalAllowanceSpecialRatePool = 200,
        enhancedCapitalAllowance = 200,
        allowanceOnSales = 200,
        zeroEmissionGoodsVehicleAllowance = 200,
        includedNonTaxableProfits = 200,
        basisAdjustment = 200,
        overlapReliefUsed = 200,
        accountingAdjustment = 200,
        averagingAdjustment = 200,
        lossBroughtForward = 200,
        outstandingBusinessIncome = 200,
        balancingChargeBPRA = 200,
        balancingChargeOther = 200,
        goodsAndServicesOwnUse = 200
      ).toString

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.annualSummaryWillBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson)
    }

    "return code 200 containing an empty object when retrieving a non-existent annual summary" in {

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.noAnnualSummaryFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/$taxYear")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .jsonBodyIsEmptyObject()
    }

    "return code 404 when retrieving an annual summary for a non-existent self-employment" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().selfEmployment.annualSummaryWillNotBeReturnedFor(nino)
        .when()
        .get(s"/ni/$nino/self-employments/abc/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }

    "return code 400 when retrieving annual summary for a non MTD year" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .get(s"/ni/$nino/self-employments/abc/2015-16")
        .thenAssertThat()
        .statusIs(400)
        .bodyIsError("TAX_YEAR_INVALID")
    }

    "return code 500 when we receive a status code from DES that we do not handle" in {
      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .des().isATeapotFor(nino)
        .when()
        .put(Jsons.SelfEmployment.update()).at(s"/ni/$nino/self-employments/abc/$taxYear")
        .thenAssertThat()
        .statusIs(500)
    }
  }

}
