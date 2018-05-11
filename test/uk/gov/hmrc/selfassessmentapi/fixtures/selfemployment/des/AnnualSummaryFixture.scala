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

package uk.gov.hmrc.selfassessmentapi.fixtures.selfemployment.des

import uk.gov.hmrc.selfassessmentapi.models.des

object AnnualSummaryFixture {

  def annualSummary() = des.selfemployment.SelfEmploymentAnnualSummary(
    Some(des.selfemployment.AnnualAdjustments(
      includedNonTaxableProfits = Some(500.25),
      basisAdjustment = Some(500.25),
      overlapReliefUsed = Some(500.25),
      accountingAdjustment = Some(500.25),
      averagingAdjustment = Some(500.25),
      lossBroughtForward = Some(500.25),
      outstandingBusinessIncome = Some(500.25),
      balancingChargeBpra = Some(500.25),
      balancingChargeOther = Some(500.25),
      goodsAndServicesOwnUse = Some(500.25),
      overlapProfitCarriedForward = Some(500.25),
      overlapProfitBroughtForward = Some(500.25),
      lossCarriedForwardTotal = Some(500.25),
      cisDeductionsTotal = Some(500.25),
      taxDeductionsFromTradingIncome = Some(500.25),
      class4NicProfitAdjustment = Some(500.25)
    )),
    Some(des.selfemployment.AnnualAllowances(
      annualInvestmentAllowance = Some(500.25),
      businessPremisesRenovationAllowance = Some(500.25),
      capitalAllowanceMainPool = Some(500.25),
      capitalAllowanceSpecialRatePool = Some(500.25),
      enhanceCapitalAllowance = Some(500.25),
      allowanceOnSales = Some(500.25),
      zeroEmissionGoodsVehicleAllowance = Some(500.25),
      capitalAllowanceSingleAssetPool = Some(500.25)
    )),
    Some(des.selfemployment.AnnualNonFinancials(
      businessDetailsChangedRecently = None,
      payClass2Nics = Some(false),
      exemptFromPayingClass4Nics = Some(true),
      class4NicsExemptionReason = Some("003")
    ))
  )
}
