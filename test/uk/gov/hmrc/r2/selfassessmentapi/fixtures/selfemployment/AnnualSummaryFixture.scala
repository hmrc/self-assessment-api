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

package uk.gov.hmrc.r2.selfassessmentapi.fixtures.selfemployment

import uk.gov.hmrc.selfassessmentapi.models.Class4NicInfo
import uk.gov.hmrc.selfassessmentapi.models.Class4NicsExemptionCode.NON_RESIDENT
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.{Adjustments, Allowances, NonFinancials, SelfEmploymentAnnualSummary}

object AnnualSummaryFixture {

  def annualSummary() = SelfEmploymentAnnualSummary(
    Some(Allowances(
      annualInvestmentAllowance = Some(50.50),
      businessPremisesRenovationAllowance = Some(20.20),
      capitalAllowanceMainPool = Some(12.34),
      capitalAllowanceSpecialRatePool = Some(55.65),
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
    Some(NonFinancials(
      class4NicInfo = Some(Class4NicInfo(
        isExempt = Some(true),
        exemptionCode = Some(NON_RESIDENT))
      ),
      payVoluntaryClass2Nic = Some(false))
    )
  )
}
