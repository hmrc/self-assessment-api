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

package uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment

import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import uk.gov.hmrc.selfassessmentapi.Generators.amountGen
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.resources.models.ErrorCode._

class SelfEmploymentAnnualSummarySpec extends JsonSpec with GeneratorDrivenPropertyChecks {

  def genAllowances(withBusinessPremisesRenovationAllowance: Boolean = true): Gen[Allowances] =
    for {
      annualInvestmentAllowance <- Gen.option[BigDecimal](amountGen(0, 2000))
      capitalAllowanceMainPool <- Gen.option[BigDecimal](amountGen(0, 2000))
      capitalAllowanceSpecialRatePool <- Gen.option[BigDecimal](amountGen(0, 2000))
      businessPremisesRenovationAllowance <- amountGen(0, 2000)
      enhancedCapitalAllowance <- Gen.option[BigDecimal](amountGen(0, 2000))
      allowanceOnSales <- Gen.option[BigDecimal](amountGen(0, 2000))
      zeroEmissionGoodsVehicleAllowance <- Gen.option[BigDecimal](amountGen(0, 2000))
    } yield
      Allowances(annualInvestmentAllowance = annualInvestmentAllowance,
                 capitalAllowanceMainPool = capitalAllowanceMainPool,
                 capitalAllowanceSpecialRatePool = capitalAllowanceSpecialRatePool,
                 businessPremisesRenovationAllowance =
                   if (withBusinessPremisesRenovationAllowance) Some(businessPremisesRenovationAllowance) else None,
                 enhancedCapitalAllowance = enhancedCapitalAllowance,
                 allowanceOnSales = allowanceOnSales,
                 zeroEmissionGoodsVehicleAllowance = zeroEmissionGoodsVehicleAllowance)

  val genAdjustments: Gen[Adjustments] = for {
    includedNonTaxableProfits <- Gen.option[BigDecimal](amountGen(0, 2000))
    basisAdjustment <- Gen.option[BigDecimal](amountGen(-2000, 2000))
    overlapReliefUsed <- Gen.option[BigDecimal](amountGen(0, 2000))
    accountingAdjustment <- Gen.option[BigDecimal](amountGen(0, 2000))
    averagingAdjustment <- Gen.option[BigDecimal](amountGen(-2000, 2000))
    lossBroughtForward <- Gen.option[BigDecimal](amountGen(0, 2000))
    outstandingBusinessIncome <- Gen.option[BigDecimal](amountGen(0, 2000))
    balancingChargeBPRA <- amountGen(0, 2000)
    balancingChargeOther <- Gen.option[BigDecimal](amountGen(0, 2000))
    goodsAndServicesOwnUse <- Gen.option[BigDecimal](amountGen(0, 2000))

  } yield
    Adjustments(includedNonTaxableProfits = includedNonTaxableProfits,
                basisAdjustment = basisAdjustment,
                overlapReliefUsed = overlapReliefUsed,
                accountingAdjustment = accountingAdjustment,
                averagingAdjustment = averagingAdjustment,
                lossBroughtForward = lossBroughtForward,
                outstandingBusinessIncome = outstandingBusinessIncome,
                balancingChargeBPRA = Some(balancingChargeBPRA),
                balancingChargeOther = balancingChargeOther,
                goodsAndServicesOwnUse = goodsAndServicesOwnUse)

  "format" should {
    val genValidAnnualSummary = for {
      allowances <- genAllowances()
      adjustments <- genAdjustments
    } yield SelfEmploymentAnnualSummary(allowances = Some(allowances), adjustments = Some(adjustments))

    "round trip Expense json" in forAll(genValidAnnualSummary) { annualSummary =>
      roundTripJson(annualSummary)
    }
  }

  "validate" should {
    val genInvalidAnnualSummary = for {
      allowances <- genAllowances(withBusinessPremisesRenovationAllowance = false)
      adjustments <- genAdjustments
    } yield SelfEmploymentAnnualSummary(allowances = Some(allowances), adjustments = Some(adjustments))

    "reject annual summaries where allowances.businessPremisesRenovationAllowance is not defined but adjustments.balancingChargeBPRA is defined" in
      forAll(genInvalidAnnualSummary) { annualSummary =>
        assertValidationErrorWithCode(annualSummary, "", INVALID_BALANCING_CHARGE_BPRA)
      }
  }

}
