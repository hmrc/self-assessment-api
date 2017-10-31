/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import uk.gov.hmrc.selfassessmentapi.models.Class4NicsExemptionCode.{NON_RESIDENT, DIVER}
import uk.gov.hmrc.selfassessmentapi.models.{Class4NicInfo, ErrorCode, des}
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class SelfEmploymentAnnualSummarySpec extends JsonSpec {
  "format" should {
    "round trip" in {
      val summary = SelfEmploymentAnnualSummary(
        Some(Allowances(
          annualInvestmentAllowance = Some(50.50),
          capitalAllowanceMainPool = Some(12.34),
          capitalAllowanceSpecialRatePool = Some(55.65),
          businessPremisesRenovationAllowance = Some(20.20),
          enhancedCapitalAllowance = Some(12.23),
          allowanceOnSales = Some(87.56),
          zeroEmissionGoodsVehicleAllowance = Some(5.33)
        )),
        Some(Adjustments(
          includedNonTaxableProfits = Some(12.22),
          basisAdjustment = Some(55.55),
          overlapReliefUsed = Some(12.23),
          accountingAdjustment = Some(12.23),
          averagingAdjustment = Some(-12.22),
          lossBroughtForward = Some(22.22),
          outstandingBusinessIncome = Some(300.33),
          balancingChargeBPRA = Some(10.55),
          balancingChargeOther = Some(5.55),
          goodsAndServicesOwnUse = Some(12.23)
        )),
        Some(NonFinancials(Some(Class4NicInfo(Some(true), Some(NON_RESIDENT))), Some(false))))

      roundTripJson(summary)
    }

    "round trip empty json" in {
      roundTripJson(SelfEmploymentAnnualSummary(None, None, None))
    }
  }

  "validate" should {
    "reject annual summaries where allowances.businessPremisesRenovationAllowance is not defined but adjustments.balancingChargeBPRA is defined" in {
      val summary = SelfEmploymentAnnualSummary(
        Some(Allowances(businessPremisesRenovationAllowance = Some(0))),
        Some(Adjustments(balancingChargeBPRA = Some(200.90))),
        None)

      assertValidationErrorWithCode(summary, "", ErrorCode.INVALID_BALANCING_CHARGE_BPRA)
    }

    "accept annual summaries with only businessPremisesRenovationAllowance defined" in {
      val summary = SelfEmploymentAnnualSummary(
        Some(Allowances(businessPremisesRenovationAllowance = Some(0))),
        None, None)

      assertValidationPasses(summary)
    }
  }


  "from" should {
    "correctly map a DES self-employment to an API self-employment" in {
      val desSelfEmployment = des.selfemployment.SelfEmploymentAnnualSummary(
        annualAdjustments = Some(des.selfemployment.AnnualAdjustments(
          includedNonTaxableProfits = Some(200.25),
          basisAdjustment = Some(200.25),
          overlapReliefUsed = Some(200.25),
          accountingAdjustment = Some(200.25),
          averagingAdjustment = Some(200.25),
          lossBroughtForward = Some(200.25),
          outstandingBusinessIncome = Some(200.25),
          balancingChargeBpra = Some(200.25),
          balancingChargeOther = Some(200.25),
          goodsAndServicesOwnUse = Some(200.25),
          overlapProfitCarriedForward = Some(10),
          adjustedProfit = Some(10.23),
          adjustedLoss = Some(20.25),
          lossOffsetAgainstOtherIncome = Some(10.15),
          lossCarriedBackOffsetAgainstIncomeOrCGT = Some(10.25),
          lossCarriedForwardTotal = Some(12.25),
          cisDeductionsTotal = Some(10.05),
          taxDeductionsFromTradingIncome = Some(12.25),
          class4NicProfitAdjustment = Some(25.10)
        )),
        annualAllowances = Some(des.selfemployment.AnnualAllowances(
          annualInvestmentAllowance = Some(200.25),
          capitalAllowanceMainPool = Some(200.25),
          capitalAllowanceSpecialRatePool = Some(200.25),
          businessPremisesRenovationAllowance = Some(200.25),
          enhanceCapitalAllowance = Some(200.25),
          allowanceOnSales = Some(200.25),
          zeroEmissionGoodsVehicleAllowance = Some(200.25),
          capitalAllowanceSingleAssetPool = Some(123.23)
        )),
        annualNonFinancials = Some(des.selfemployment.AnnualNonFinancials(
          businessDetailsChangedRecently = None,
          payClass2Nics = Some(false),
          exemptFromPayingClass4Nics = Some(true),
          class4NicsExemptionReason = Some("003")
        ))
      )

      val apiSummary = SelfEmploymentAnnualSummary.from(desSelfEmployment)
      val adjustments = apiSummary.adjustments.get

      adjustments.includedNonTaxableProfits shouldBe Some(200.25)
      adjustments.basisAdjustment shouldBe Some(200.25)
      adjustments.overlapReliefUsed shouldBe Some(200.25)
      adjustments.accountingAdjustment shouldBe Some(200.25)
      adjustments.averagingAdjustment shouldBe Some(200.25)
      adjustments.lossBroughtForward shouldBe Some(200.25)
      adjustments.outstandingBusinessIncome shouldBe Some(200.25)
      adjustments.balancingChargeBPRA shouldBe Some(200.25)
      adjustments.balancingChargeOther shouldBe Some(200.25)
      adjustments.goodsAndServicesOwnUse shouldBe Some(200.25)
      adjustments.overlapProfitCarriedForward shouldBe Some(10)
      adjustments.adjustedProfit shouldBe Some(10.23)
      adjustments.adjustedLoss shouldBe Some(20.25)
      adjustments.lossOffsetAgainstOtherIncome shouldBe Some(10.15)
      adjustments.lossCarriedBackOffsetAgainstIncomeOrCGT shouldBe Some(10.25)
      adjustments.lossCarriedForwardTotal shouldBe Some(12.25)
      adjustments.cisDeductionsTotal shouldBe Some(10.05)
      adjustments.taxDeductionsFromTradingIncome shouldBe Some(12.25)
      adjustments.class4NicProfitAdjustment shouldBe Some(25.10)

      val allowances = apiSummary.allowances.get

      allowances.annualInvestmentAllowance shouldBe Some(200.25)
      allowances.capitalAllowanceMainPool shouldBe Some(200.25)
      allowances.capitalAllowanceSpecialRatePool shouldBe Some(200.25)
      allowances.businessPremisesRenovationAllowance shouldBe Some(200.25)
      allowances.enhancedCapitalAllowance shouldBe Some(200.25)
      allowances.allowanceOnSales shouldBe Some(200.25)
      allowances.zeroEmissionGoodsVehicleAllowance shouldBe Some(200.25)
      allowances.capitalAllowanceSingleAssetPool shouldBe Some(123.23)

      val nonFinancials = apiSummary.nonFinancials.get
      nonFinancials.class4NicInfo.get.exemptionCode shouldBe Some(DIVER)
      nonFinancials.class4NicInfo.get.isExempt shouldBe Some(true)
      nonFinancials.payVoluntaryClass2Nic shouldBe Some(false)
    }
  }
}
