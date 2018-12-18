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

import uk.gov.hmrc.r2.selfassessmentapi.models.ErrorCode.INVALID_MONETARY_AMOUNT
import uk.gov.hmrc.r2.selfassessmentapi.resources.JsonSpec

class AllowancesSpec extends JsonSpec {

  "format" should {
    "round trip valid Allowances json" in {
      roundTripJson(Allowances(
        annualInvestmentAllowance = Some(10.00),
        businessPremisesRenovationAllowance = Some(200.50),
        capitalAllowanceMainPool = Some(10.00),
        capitalAllowanceSpecialRatePool = Some(10.00),
        zeroEmissionGoodsVehicleAllowance = Some(10.00),
        enhancedCapitalAllowance = Some(10.00),
        allowanceOnSales = Some(10.00),
        capitalAllowanceSingleAssetPool = Some(10.00),
        tradingAllowance = Some(10.00)))
    }

    "round trip Allowances with no fields" in {
      roundTripJson(Allowances())
    }
  }

  "validate" should {
    def validateAmount(model: Allowances, fieldName: String) = {
      assertValidationErrorWithCode(
        model,
        fieldName, INVALID_MONETARY_AMOUNT)
    }

    "reject negative annualInvestmentAllowance" in {
      val se = Allowances(annualInvestmentAllowance = Some(-10.00))
      validateAmount(se, "/annualInvestmentAllowance")
    }

    "reject annualInvestmentAllowance more than 99999999999999.98" in {
      val se = Allowances(annualInvestmentAllowance = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/annualInvestmentAllowance")
    }

    "reject annualInvestmentAllowance with more than 2 decimal places" in {
      val se = Allowances(annualInvestmentAllowance = Some(10.123))
      validateAmount(se, "/annualInvestmentAllowance")
    }

    "reject negative businessPremisesRenovationAllowance" in {
      val se = Allowances(businessPremisesRenovationAllowance = Some(-10.00))
      validateAmount(se, "/businessPremisesRenovationAllowance")
    }

    "reject businessPremisesRenovationAllowance more than 99999999999999.98" in {
      val se = Allowances(businessPremisesRenovationAllowance = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/businessPremisesRenovationAllowance")
    }

    "reject businessPremisesRenovationAllowance with more than 2 decimal places" in {
      val se = Allowances(businessPremisesRenovationAllowance = Some(10.123))
      validateAmount(se, "/businessPremisesRenovationAllowance")
    }


    "reject negative capitalAllowanceMainPool" in {
      val se = Allowances(capitalAllowanceMainPool = Some(-10.00))
      validateAmount(se, "/capitalAllowanceMainPool")
    }

    "reject capitalAllowanceMainPool more than 99999999999999.98" in {
      val se = Allowances(capitalAllowanceMainPool = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/capitalAllowanceMainPool")
    }

    "reject capitalAllowanceMainPool with more than 2 decimal places" in {
      val se = Allowances(capitalAllowanceMainPool = Some(10.123))
      validateAmount(se, "/capitalAllowanceMainPool")
    }

    "reject negative capitalAllowanceSpecialRatePool" in {
      val se = Allowances(capitalAllowanceSpecialRatePool = Some(-10.00))
      validateAmount(se, "/capitalAllowanceSpecialRatePool")
    }

    "reject capitalAllowanceSpecialRatePool more than 99999999999999.98" in {
      val se = Allowances(capitalAllowanceSpecialRatePool = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/capitalAllowanceSpecialRatePool")
    }

    "reject capitalAllowanceSpecialRatePool with more than 2 decimal places" in {
      val se = Allowances(capitalAllowanceSpecialRatePool = Some(10.123))
      validateAmount(se, "/capitalAllowanceSpecialRatePool")
    }

    "reject negative enhancedCapitalAllowance" in {
      val se = Allowances(enhancedCapitalAllowance = Some(-10.00))
      validateAmount(se, "/enhancedCapitalAllowance")
    }

    "reject enhancedCapitalAllowance more than 99999999999999.98" in {
      val se = Allowances(enhancedCapitalAllowance = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/enhancedCapitalAllowance")
    }

    "reject enhancedCapitalAllowance with more than 2 decimal places" in {
      val se = Allowances(enhancedCapitalAllowance = Some(10.123))
      validateAmount(se, "/enhancedCapitalAllowance")
    }

    "reject negative allowanceOnSales" in {
      val se = Allowances(allowanceOnSales = Some(-10.00))
      validateAmount(se, "/allowanceOnSales")
    }

    "reject allowanceOnSales more than 99999999999999.98" in {
      val se = Allowances(allowanceOnSales = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/allowanceOnSales")
    }

    "reject allowanceOnSales with more than 2 decimal places" in {
      val se = Allowances(allowanceOnSales = Some(10.123))
      validateAmount(se, "/allowanceOnSales")
    }

    "reject negative zeroEmissionGoodsVehicleAllowance" in {
      val se = Allowances(zeroEmissionGoodsVehicleAllowance = Some(-10))
      validateAmount(se, "/zeroEmissionGoodsVehicleAllowance")
    }

    "reject zeroEmissionGoodsVehicleAllowance more than 99999999999999.98" in {
      val se = Allowances(zeroEmissionGoodsVehicleAllowance = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/zeroEmissionGoodsVehicleAllowance")
    }

    "reject zeroEmissionGoodsVehicleAllowance with more than 2 decimal places" in {
      val se = Allowances(zeroEmissionGoodsVehicleAllowance = Some(10.123))
      validateAmount(se, "/zeroEmissionGoodsVehicleAllowance")
    }

    "reject negative capitalAllowanceSingleAssetPool" in {
      val se = Allowances(capitalAllowanceSingleAssetPool = Some(-10.00))
      validateAmount(se, "/capitalAllowanceSingleAssetPool" )
    }

    "reject capitalAllowanceSingleAssetPool more than 99999999999999.98" in {
      val se = Allowances(capitalAllowanceSingleAssetPool = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/capitalAllowanceSingleAssetPool")
    }

    "reject capitalAllowanceSingleAssetPool with more than 2 decimal places" in {
      val se = Allowances(capitalAllowanceSingleAssetPool = Some(10.123))
      validateAmount(se, "/capitalAllowanceSingleAssetPool")
    }

    "reject negative tradingAllowance" in {
      val se = Allowances(tradingAllowance = Some(-10.00))
      validateAmount(se, "/tradingAllowance")
    }

    "reject tradingAllowance more than 99999999999999.98" in {
      val se = Allowances(tradingAllowance = Some(BigDecimal("99999999999999.99")))
      validateAmount(se, "/tradingAllowance")
    }

    "reject trading allowance with more than 2 decimal places" in {
      val se = Allowances(tradingAllowance = Some(10.123))
      validateAmount(se, "/tradingAllowance")
    }
  }
}
