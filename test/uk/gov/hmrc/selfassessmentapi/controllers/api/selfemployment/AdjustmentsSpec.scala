/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment

import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode
import ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec

class AdjustmentsSpec extends JsonSpec {

  "format" should {
    "round trip valid Adjustments json" in {
      roundTripJson(Adjustments.example)
    }
  }

  "validate" should {
    def validateNegative(model: Adjustments, fieldName: String) = {
      assertValidationError[Adjustments](
        model,
        Map(fieldName -> INVALID_MONETARY_AMOUNT), "Expected valid self-employment-adjustments")
    }

    "reject negative includedNonTaxableProfits" in {
      val se = Adjustments(
        includedNonTaxableProfits = BigDecimal(-10.00),
        basisAdjustment = 0,
        overlapReliefUsed = 0,
        accountingAdjustment = 0,
        averagingAdjustment = 0,
        lossBroughtForward = 0,
        outstandingBusinessIncome = 0)

     validateNegative(se, "/includedNonTaxableProfits")
    }

    "reject negative overlapReliefUsed" in {
      val se = Adjustments(
        includedNonTaxableProfits = 0,
        basisAdjustment = 0,
        overlapReliefUsed = BigDecimal(-10.00),
        accountingAdjustment = 0,
        averagingAdjustment = 0,
        lossBroughtForward = 0,
        outstandingBusinessIncome = 0)

     validateNegative(se, "/overlapReliefUsed")
    }

    "reject negative accountingAdjustment" in {
      val se = Adjustments(
        includedNonTaxableProfits = 0,
        basisAdjustment = 0,
        overlapReliefUsed = 0,
        accountingAdjustment = BigDecimal(-10.00),
        averagingAdjustment = 0,
        lossBroughtForward = 0,
        outstandingBusinessIncome = 0)

     validateNegative(se, "/accountingAdjustment")
    }

    "not reject negative averagingAdjustment" in {
      val se = Adjustments(
        includedNonTaxableProfits = 0,
        basisAdjustment = 0,
        overlapReliefUsed = 0,
        accountingAdjustment = 0,
        averagingAdjustment = BigDecimal(-10.00),
        lossBroughtForward = 0,
        outstandingBusinessIncome = 0)

     assertValidationPasses[Adjustments](se)
    }

    "not reject positive averagingAdjustment" in {
      val se = Adjustments(
        includedNonTaxableProfits = 0,
        basisAdjustment = 0,
        overlapReliefUsed = 0,
        accountingAdjustment = 0,
        averagingAdjustment = BigDecimal(10.00),
        lossBroughtForward = 0,
        outstandingBusinessIncome = 0)

     assertValidationPasses[Adjustments](se)
    }

    "reject negative lossBroughtForward" in {
      val se = Adjustments(
        includedNonTaxableProfits = 0,
        basisAdjustment = 0,
        overlapReliefUsed = 0,
        accountingAdjustment = 0,
        averagingAdjustment = 0,
        lossBroughtForward = BigDecimal(-10.00),
        outstandingBusinessIncome = 0)

     validateNegative(se, "/lossBroughtForward")
    }

    "reject negative outstandingBusinessIncome" in {
      val se = Adjustments(
        includedNonTaxableProfits = 0,
        basisAdjustment = 0,
        overlapReliefUsed = 0,
        accountingAdjustment = 0,
        averagingAdjustment = 0,
        lossBroughtForward = 0,
        outstandingBusinessIncome = BigDecimal(-10.00))

     validateNegative(se, "/outstandingBusinessIncome")
    }

    "not reject negative basisAdjustment" in {
      val se = Adjustments(
        includedNonTaxableProfits = 0,
        basisAdjustment = BigDecimal(-10.00),
        overlapReliefUsed = 0,
        accountingAdjustment = 0,
        averagingAdjustment = 0,
        lossBroughtForward = 0,
        outstandingBusinessIncome = 0)

      assertValidationPasses[Adjustments](se)
    }

    "not reject positive basisAdjustment" in {
      val se = Adjustments(
        includedNonTaxableProfits = 0,
        basisAdjustment = BigDecimal(10.00),
        overlapReliefUsed = 0,
        accountingAdjustment = 0,
        averagingAdjustment = 0,
        lossBroughtForward = 0,
        outstandingBusinessIncome = 0)

      assertValidationPasses[Adjustments](se)
    }

  }
}
