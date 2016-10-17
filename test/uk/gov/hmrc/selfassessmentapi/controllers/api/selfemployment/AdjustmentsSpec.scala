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
      val se = Adjustments(includedNonTaxableProfits = Some(BigDecimal(-10.00)))
     validateNegative(se, "/includedNonTaxableProfits")
    }

    "reject negative overlapReliefUsed" in {
      val se = Adjustments(overlapReliefUsed = Some(BigDecimal(-10.00)))
     validateNegative(se, "/overlapReliefUsed")
    }

    "reject negative accountingAdjustment" in {
      val se = Adjustments(accountingAdjustment = Some(BigDecimal(-10.00)))
     validateNegative(se, "/accountingAdjustment")
    }

    "not reject negative averagingAdjustment" in {
      val se = Adjustments(averagingAdjustment = Some(BigDecimal(-10.00)))
     assertValidationPasses[Adjustments](se)
    }

    "not reject positive averagingAdjustment" in {
      val se = Adjustments(averagingAdjustment = Some(BigDecimal(10.00)))
     assertValidationPasses[Adjustments](se)
    }

    "reject negative lossBroughtForward" in {
      val se = Adjustments(lossBroughtForward = Some(BigDecimal(-10.00)))
     validateNegative(se, "/lossBroughtForward")
    }

    "reject negative outstandingBusinessIncome" in {
      val se = Adjustments(outstandingBusinessIncome = Some(BigDecimal(-10.00)))
     validateNegative(se, "/outstandingBusinessIncome")
    }

    "not reject negative basisAdjustment" in {
      val se = Adjustments(basisAdjustment = Some(BigDecimal(-10.00)))
      assertValidationPasses[Adjustments](se)
    }

    "not reject positive basisAdjustment" in {
      val se = Adjustments(basisAdjustment = Some(BigDecimal(10.00)))
      assertValidationPasses[Adjustments](se)
    }

  }
}
