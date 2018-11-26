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

package uk.gov.hmrc.r2.selfassessmentapi.models.selfemployment

import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode.INVALID_MONETARY_AMOUNT

class AdjustmentsSpec extends JsonSpec {

  "format" should {
    "round trip valid Adjustments json" in {
      roundTripJson(Adjustments(
        includedNonTaxableProfits = Some(BigDecimal(10.00)),
        basisAdjustment = Some(BigDecimal(10.00)),
        overlapReliefUsed = Some(BigDecimal(10.00)),
        accountingAdjustment = Some(BigDecimal(10.00)),
        averagingAdjustment = Some(BigDecimal(10.00)),
        lossBroughtForward = Some(BigDecimal(10.00)),
        outstandingBusinessIncome = Some(BigDecimal(10.00))))
    }

    "round trip Adjustments with no fields" in {
      roundTripJson(Adjustments())
    }
  }

  "validate" should {
    def validateNonNegativeAmount(model: Adjustments, path: String) = {
      assertValidationErrorWithCode(model, path,
        INVALID_MONETARY_AMOUNT)
    }

    def validateMaxAmount(model: Adjustments, path: String) = {
      assertValidationErrorWithCode(model, path, INVALID_MONETARY_AMOUNT)
    }

    def validateAmount(model: Adjustments, path: String) = {
      assertValidationErrorWithCode(model, path,
        INVALID_MONETARY_AMOUNT)
    }

    "reject negative includedNonTaxableProfits" in {
      val se = Adjustments(includedNonTaxableProfits = Some(BigDecimal(-10.00)))
      validateNonNegativeAmount(se, "/includedNonTaxableProfits")
    }

    "reject includedNonTaxableProfits more than 99999999999999.98" in {
      val se = Adjustments(includedNonTaxableProfits = Some(BigDecimal("99999999999999.99")))
      validateMaxAmount(se, "/includedNonTaxableProfits")
    }

    "reject includedNonTaxableProfits with more than two decimal places" in {
      val se = Adjustments(includedNonTaxableProfits = Some(BigDecimal(10.123)))
      validateNonNegativeAmount(se, "/includedNonTaxableProfits")
    }

    "reject basisAdjustment with more than two decimal places" in {
      val se = Adjustments(basisAdjustment = Some(BigDecimal(10.123)))
      validateAmount(se, "/basisAdjustment")
    }

    "reject basisAdjustment more than 99999999999999.98" in {
      val se = Adjustments(basisAdjustment = Some(BigDecimal("99999999999999.99")))
      validateMaxAmount(se, "/basisAdjustment")
    }

    "reject negative overlapReliefUsed" in {
      val se = Adjustments(overlapReliefUsed = Some(BigDecimal(-10.00)))
     validateNonNegativeAmount(se, "/overlapReliefUsed")
    }

    "reject overlapReliefUsed more than 99999999999999.98" in {
      val se = Adjustments(overlapReliefUsed = Some(BigDecimal("99999999999999.99")))
      validateMaxAmount(se, "/overlapReliefUsed")
    }

    "reject overlapReliefUsed with more than two decimal places" in {
      val se = Adjustments(overlapReliefUsed = Some(BigDecimal(10.123)))
      validateNonNegativeAmount(se, "/overlapReliefUsed")
    }

    "reject negative accountingAdjustment" in {
      val se = Adjustments(accountingAdjustment = Some(BigDecimal(-10.00)))
      validateNonNegativeAmount(se, "/accountingAdjustment")
    }

    "reject accountingAdjustment more than 99999999999999.98" in {
      val se = Adjustments(accountingAdjustment = Some(BigDecimal("99999999999999.99")))
      validateMaxAmount(se, "/accountingAdjustment")
    }

    "reject accountingAdjustment with more than two decimal places" in {
      val se = Adjustments(accountingAdjustment = Some(BigDecimal(10.123)))
      validateNonNegativeAmount(se, "/accountingAdjustment")
    }

    "reject averagingAdjustment with more than two decimal places" in {
      val se = Adjustments(averagingAdjustment = Some(BigDecimal(10.123)))
      validateAmount(se, "/averagingAdjustment")
    }

    "reject averagingAdjustment more than 99999999999999.98" in {
      val se = Adjustments(averagingAdjustment = Some(BigDecimal("99999999999999.99")))
      validateMaxAmount(se, "/averagingAdjustment")
    }

    "reject negative lossBroughtForward" in {
      val se = Adjustments(lossBroughtForward = Some(BigDecimal(-10.00)))
      validateNonNegativeAmount(se, "/lossBroughtForward")
    }

    "reject lossBroughtForward more than 99999999999999.98" in {
      val se = Adjustments(lossBroughtForward = Some(BigDecimal("99999999999999.99")))
      validateMaxAmount(se, "/lossBroughtForward")
    }

    "reject lossBroughtForward with more than two decimal places" in {
      val se = Adjustments(lossBroughtForward = Some(BigDecimal(10.123)))
      validateNonNegativeAmount(se, "/lossBroughtForward")
    }

    "reject negative outstandingBusinessIncome" in {
      val se = Adjustments(outstandingBusinessIncome = Some(BigDecimal(-10.00)))
      validateNonNegativeAmount(se, "/outstandingBusinessIncome")
    }

    "reject outstandingBusinessIncome more than 99999999999999.98" in {
      val se = Adjustments(outstandingBusinessIncome = Some(BigDecimal("99999999999999.99")))
      validateMaxAmount(se, "/outstandingBusinessIncome")
    }

    "reject outstandingBusinessIncome with more than two decimal places" in {
      val se = Adjustments(outstandingBusinessIncome = Some(BigDecimal(10.123)))
      validateNonNegativeAmount(se, "/outstandingBusinessIncome")
    }

    "return a INVALID_MONETARY_AMOUNT error when balancingCharge amount is negative" in {
      val period = Adjustments(balancingChargeBPRA = Some(BigDecimal(-100)))

      assertValidationErrorWithCode(period, "/balancingChargeBPRA", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject balancingChargeBPRA more than 99999999999999.98" in {
      val se = Adjustments(balancingChargeBPRA = Some(BigDecimal("99999999999999.99")))
      validateMaxAmount(se, "/balancingChargeBPRA")
    }

    "return a INVALID_MONETARY_AMOUNT error when balancingCharge amount contains more than 2 decimal places" in {
      val period = Adjustments(balancingChargeOther = Some(BigDecimal(100.555)))

      assertValidationErrorWithCode(period, "/balancingChargeOther", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return a INVALID_MONETARY_AMOUNT error when goodsAndServicesOwnUse is negative" in {
      val period = Adjustments(goodsAndServicesOwnUse = Some(-200))

      assertValidationErrorWithCode(period, "/goodsAndServicesOwnUse", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject goodsAndServicesOwnUse more than 99999999999999.98" in {
      val se = Adjustments(goodsAndServicesOwnUse = Some(BigDecimal("99999999999999.99")))
      validateMaxAmount(se, "/goodsAndServicesOwnUse")
    }

    "return a INVALID_MONETARY_AMOUNT error when goodsAndServicesOwnUse contains more than two decimal places" in {
      val period = Adjustments(goodsAndServicesOwnUse = Some(200.123))

      assertValidationErrorWithCode(period, "/goodsAndServicesOwnUse", ErrorCode.INVALID_MONETARY_AMOUNT)
    }
  }
}
