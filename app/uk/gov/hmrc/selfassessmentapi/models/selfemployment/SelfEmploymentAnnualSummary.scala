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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models.des

case class SelfEmploymentAnnualSummary(allowances: Option[Allowances],
                                       adjustments: Option[Adjustments],
                                       nonFinancials: Option[NonFinancials])

object SelfEmploymentAnnualSummary {
  implicit val writes: Writes[SelfEmploymentAnnualSummary] = Json.writes[SelfEmploymentAnnualSummary]

  implicit val reads: Reads[SelfEmploymentAnnualSummary] = (
    (__ \ "allowances").readNullable[Allowances] and
      (__ \ "adjustments").readNullable[Adjustments] and
      (__ \ "nonFinancials").readNullable[NonFinancials]
    ) (SelfEmploymentAnnualSummary.apply _)

  def from(desSummary: des.selfemployment.SelfEmploymentAnnualSummary): SelfEmploymentAnnualSummary = {
    val adjustments = desSummary.annualAdjustments.map { adj =>
      Adjustments(
        includedNonTaxableProfits = adj.includedNonTaxableProfits,
        basisAdjustment = adj.basisAdjustment,
        overlapReliefUsed = adj.overlapReliefUsed,
        accountingAdjustment = adj.accountingAdjustment,
        averagingAdjustment = adj.averagingAdjustment,
        lossBroughtForward = adj.lossBroughtForward,
        outstandingBusinessIncome = adj.outstandingBusinessIncome,
        balancingChargeBPRA = adj.balancingChargeBpra,
        balancingChargeOther = adj.balancingChargeOther,
        goodsAndServicesOwnUse = adj.goodsAndServicesOwnUse,
        overlapProfitCarriedForward = adj.overlapProfitCarriedForward,
        overlapProfitBroughtForward = adj.overlapProfitBroughtForward,
        lossCarriedForwardTotal = adj.lossCarriedForwardTotal,
        cisDeductionsTotal = adj.cisDeductionsTotal,
        taxDeductionsFromTradingIncome = adj.taxDeductionsFromTradingIncome,
        class4NicProfitAdjustment = adj.class4NicProfitAdjustment

      )
    }

    val allowances = desSummary.annualAllowances.map { allow =>
      Allowances(
        annualInvestmentAllowance = allow.annualInvestmentAllowance,
        businessPremisesRenovationAllowance = allow.businessPremisesRenovationAllowance,
        capitalAllowanceMainPool = allow.capitalAllowanceMainPool,
        capitalAllowanceSpecialRatePool = allow.capitalAllowanceSpecialRatePool,
        enhancedCapitalAllowance = allow.enhanceCapitalAllowance,
        allowanceOnSales = allow.allowanceOnSales,
        zeroEmissionGoodsVehicleAllowance = allow.zeroEmissionGoodsVehicleAllowance,
        capitalAllowanceSingleAssetPool = allow.capitalAllowanceSingleAssetPool
      )
    }

    val nonFinancials = NonFinancials.from(desSummary.annualNonFinancials)

    SelfEmploymentAnnualSummary(allowances, adjustments, nonFinancials)
  }
}
