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

package uk.gov.hmrc.selfassessmentapi.domain.selfemployment

import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain.JsonSpec

class AllowancesSpec extends JsonSpec {

  "format" should {
    "round trip valid Allowances json" in {
      roundTripJson(Allowances(
        annualInvestmentAllowance = Some(BigDecimal(10.00)),
        capitalAllowanceMainPool = Some(BigDecimal(10.00)),
        capitalAllowanceSpecialRatePool = Some(BigDecimal(10.00)),
        restrictedCapitalAllowance = Some(BigDecimal(10.00)),
        businessPremisesRenovationAllowance = Some(BigDecimal(10.00)),
        enhancedCapitalAllowance = Some(BigDecimal(10.00)),
        allowancesOnSales = Some(BigDecimal(10.00))))
    }
  }

  "validate" should {
    def validateNegative(model: Allowances, fieldName: String) = {
      assertValidationError[Allowances](
        model,
        Map(fieldName -> INVALID_MONETARY_AMOUNT), "Expected valid self-employment-allowance")
    }

    "reject negative annualInvestmentAllowance" in {
      val se = Allowances(annualInvestmentAllowance = Some(BigDecimal(-10.00)))
     validateNegative(se, "/annualInvestmentAllowance")
    }

    "reject negative capitalAllowanceMainPool" in {
      val se = Allowances(capitalAllowanceMainPool = Some(BigDecimal(-10.00)))
     validateNegative(se, "/capitalAllowanceMainPool")
    }

    "reject negative capitalAllowanceSpecialRatePool" in {
      val se = Allowances(capitalAllowanceSpecialRatePool = Some(BigDecimal(-10.00)))
     validateNegative(se, "/capitalAllowanceSpecialRatePool")
    }

    "reject negative restrictedCapitalAllowance" in {
      val se = Allowances(restrictedCapitalAllowance = Some(BigDecimal(-10.00)))
     validateNegative(se, "/restrictedCapitalAllowance")
    }

    "reject negative businessPremisesRenovationAllowance" in {
      val se = Allowances(businessPremisesRenovationAllowance = Some(BigDecimal(-10.00)))
     validateNegative(se, "/businessPremisesRenovationAllowance")
    }

    "reject negative enhancedCapitalAllowance" in {
      val se = Allowances(enhancedCapitalAllowance = Some(BigDecimal(-10.00)))
     validateNegative(se, "/enhancedCapitalAllowance")
    }

    "reject negative allowancesOnSales" in {
      val se = Allowances(allowancesOnSales = Some(BigDecimal(-10.00)))
     validateNegative(se, "/allowancesOnSales")
    }

  }
}
