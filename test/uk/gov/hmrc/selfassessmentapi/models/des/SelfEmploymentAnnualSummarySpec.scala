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

package uk.gov.hmrc.selfassessmentapi.models.des

import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.models

class SelfEmploymentAnnualSummarySpec extends UnitSpec {
  "from" should {
    "correctly map an API self-employment annual summary to a DES self-employment annual summary" in {
      val apiSummary = models.selfemployment.SelfEmploymentAnnualSummary(
        Some(models.selfemployment.Allowances(
          annualInvestmentAllowance = Some(200.50),
          capitalAllowanceMainPool = Some(200.50),
          capitalAllowanceSpecialRatePool = Some(200.50),
          businessPremisesRenovationAllowance = Some(200.50),
          enhancedCapitalAllowance = Some(200.50),
          allowanceOnSales = Some(200.50),
          zeroEmissionGoodsVehicleAllowance = Some(200.50))),
        adjustments = Some(models.selfemployment.Adjustments(
          includedNonTaxableProfits = Some(200.50),
          basisAdjustment = Some(200.50),
          overlapReliefUsed = Some(200.50),
          accountingAdjustment = Some(200.50),
          averagingAdjustment = Some(200.50),
          lossBroughtForward = Some(200.50),
          outstandingBusinessIncome = Some(200.50),
          balancingChargeBPRA = Some(200.50),
          balancingChargeOther = Some(200.50),
          goodsAndServicesOwnUse = Some(200.50))))

      val desSummary: SelfEmploymentAnnualSummary = SelfEmploymentAnnualSummary.from(apiSummary)
      val allowances = desSummary.annualAllowances.get
      val adjustments = desSummary.annualAdjustments.get

      allowances.annualInvestmentAllowance shouldBe Some(200.50)
      allowances.capitalAllowanceMainPool shouldBe Some(200.50)
      allowances.capitalAllowanceSpecialRatePool shouldBe Some(200.50)
      allowances.businessPremisesRenovationAllowance shouldBe Some(200.50)
      allowances.enhanceCapitalAllowance shouldBe Some(200.50)
      allowances.allowanceOnSales shouldBe Some(200.50)
      allowances.zeroEmissionGoodsVehicleAllowance shouldBe Some(200.50)

      adjustments.includedNonTaxableProfits shouldBe Some(200.50)
      adjustments.basisAdjustment shouldBe Some(200.50)
      adjustments.overlapReliefUsed shouldBe Some(200.50)
      adjustments.accountingAdjustment shouldBe Some(200.50)
      adjustments.averagingAdjustment shouldBe Some(200.50)
      adjustments.lossBroughtForward shouldBe Some(200.50)
      adjustments.outstandingBusinessIncome shouldBe Some(200.50)
      adjustments.balancingChargeBpra shouldBe Some(200.50)
      adjustments.balancingChargeOther shouldBe Some(200.50)
      adjustments.goodsAndServicesOwnUse shouldBe Some(200.50)
    }
  }
}
