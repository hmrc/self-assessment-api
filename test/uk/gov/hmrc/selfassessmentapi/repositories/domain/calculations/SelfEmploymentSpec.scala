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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.calculations

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SelfAssessment, SelfEmploymentIncome}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.builders.SelfEmploymentBuilder
import uk.gov.hmrc.selfassessmentapi.repositories.domain.calculations

class SelfEmploymentSpec extends UnitSpec {

  private val selfEmploymentId = "selfEmploymentId"

  "TotalTaxableProfit" should {
    "be TotalProfit - TotalDeduction" in {
      calculations.SelfEmployment.TotalTaxableProfit(totalProfit = 10000, totalDeduction = 5000) shouldBe 5000
    }

    "be 0 if TotalProfit is less than TotalDeductions" in {
      calculations.SelfEmployment.TotalTaxableProfit(totalProfit = 1000, totalDeduction = 1001) shouldBe 0
    }
  }

  "TotalProfit" should {
    "be sum of profits from across all self employments" in {
      val selfEmploymentOne = SelfEmploymentBuilder()
        .withTurnover(2000)
        .create()

      val selfEmploymentTwo = SelfEmploymentBuilder()
        .withTurnover(3000)
        .create()

      calculations.SelfEmployment.TotalProfit(SelfAssessment(selfEmployments = Seq(selfEmploymentOne, selfEmploymentTwo))) shouldBe BigDecimal(5000)
    }
  }


  "Profit from self employment" should {
    "be equal to the sum of all incomes, balancingCharges, goodsAndServices and basisAdjustment, accountingAdjustment and " +
      "averagingAdjustment" in {

      /*val selfEmployment =
        aSelfEmployment(selfEmploymentId).copy(
          incomes = Seq(
            anIncome(IncomeType.Turnover, 1200.01),
            anIncome(IncomeType.Other, 799.99)
          ),
          balancingCharges = Seq(
            aBalancingCharge(BalancingChargeType.BPRA, 10),
            aBalancingCharge(BalancingChargeType.Other, 20)
          ),
          goodsAndServicesOwnUse = Seq(
            aGoodsAndServices(50)
          ),
          adjustments = Some(Adjustments(
            basisAdjustment = Some(200),
            accountingAdjustment = Some(100),
            averagingAdjustment = Some(50)
          ))
        )*/

      val selfEmployment = SelfEmploymentBuilder()
        .withTurnover(1200.01)
        .withOtherIncome(799.99)
        .withBpraBalancingCharges(10)
        .withOtherBalancingCharges(20)
        .goodsAndServicesOwnUse(50)
        .withAdjustments(
          basisAdjustment = 200,
          outstandingBusinessIncome = 0,
          lossBroughtForward = 0,
          averagingAdjustment = 50,
          overlapReliefUsed = 0,
          includedNonTaxableProfits = 0,
          accountingAdjustment = 100)
        .create()

      calculations.SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(2430)

    }

    "be equal to incomes and outstandingBusinessIncome" in {

      val selfEmployment = SelfEmploymentBuilder()
        .withTurnover(10000.98)
        .withAdjustments(
          outstandingBusinessIncome = 5000.32,
          lossBroughtForward = 20000.50,
          averagingAdjustment = 0,
          overlapReliefUsed = 0,
          basisAdjustment = 0,
          includedNonTaxableProfits = 0,
          accountingAdjustment = 0)
        .create()

      calculations.SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(15001)

    }

    "not contain any expenses apart from depreciation" in {

      val selfEmployment = SelfEmploymentBuilder()
        .withTurnover(2000)
        .withAdminCosts(100)
        .withBadDebt(50.01)
        .withCISPaymentsToSubcontractors(49.99)
        .withDepreciation(1000000)
        .create()

      calculations.SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(1800)
    }

    "subtract all allowances from profit" in {

      val selfEmployment = SelfEmploymentBuilder()
        .withTurnover(2000)
        .withAllowances(
          annualInvestmentAllowance = 50,
          capitalAllowanceMainPool = 10,
          capitalAllowanceSpecialRatePool = 10,
          businessPremisesRenovationAllowance = 10,
          enhancedCapitalAllowance = 4.99,
          allowancesOnSales = 5.01)
        .create()


      calculations.SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(1910)

    }

    "be rounded down to the nearest pound" in {

      val selfEmployment = SelfEmploymentBuilder()
        .withTurnover(1.99)
        .withAllowances(
          annualInvestmentAllowance = 0.02,
          allowancesOnSales = 0,
          enhancedCapitalAllowance = 0,
          businessPremisesRenovationAllowance = 0,
          capitalAllowanceSpecialRatePool = 0,
          capitalAllowanceMainPool = 0)
        .create()

      calculations.SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(1)
    }

    "subtract adjustments from profit" in {

      val selfEmployment = SelfEmploymentBuilder()
        .withTurnover(2000)
        .withAdjustments(
          includedNonTaxableProfits = 50,
          basisAdjustment = -15,
          overlapReliefUsed = 10,
          averagingAdjustment = -25,
          outstandingBusinessIncome = 0,
          lossBroughtForward = 0,
          accountingAdjustment = 0)
        .create()

      calculations.SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(1900)
    }

    "reduce cap annualInvestmentAllowance at 200000" in {

      val selfEmployment = SelfEmploymentBuilder()
        .withTurnover(230000)
        .withAllowances(
          annualInvestmentAllowance = 230000,
          allowancesOnSales = 0,
          enhancedCapitalAllowance = 0,
          businessPremisesRenovationAllowance = 0,
          capitalAllowanceSpecialRatePool = 0,
          capitalAllowanceMainPool = 0)
        .create()

      calculations.SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(30000)

    }

    "be sum of adjusted profits and outstandingBusinessIncome" in {

      val selfEmployment = SelfEmploymentBuilder()
        .withTurnover(2000)
        .withAdjustments(
          outstandingBusinessIncome = 1000,
          lossBroughtForward = 0,
          averagingAdjustment = 0,
          overlapReliefUsed = 0,
          basisAdjustment = 0,
          includedNonTaxableProfits = 0,
          accountingAdjustment = 0)
        .create()

      calculations.SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(3000)
    }

    "be zero if expenses are bigger than incomes (loss)" in {

      val selfEmployment = SelfEmploymentBuilder()
        .withTurnover(2000)
        .withAdminCosts(4000)
        .withAdjustments(
          lossBroughtForward = 1000,
          outstandingBusinessIncome = 0,
          averagingAdjustment = 0,
          overlapReliefUsed = 0,
          basisAdjustment = 0,
          includedNonTaxableProfits = 0,
          accountingAdjustment = 0)
        .create()

      calculations.SelfEmployment.Profit(selfEmployment) shouldBe BigDecimal(0)
    }

  }

  "SelfEmployment.TaxableProfit" should {
    "be RoundDown(Profit - CappedAt(LossBroughtForward, AdjustedProfit))" in {
      val selfEmployment = SelfEmploymentBuilder()
        .withTurnover(10000.98)
        .withAdjustments(
          outstandingBusinessIncome = 5000.32,
          lossBroughtForward = 20000.50,
          averagingAdjustment = 0,
          overlapReliefUsed = 0,
          basisAdjustment = 0,
          includedNonTaxableProfits = 0,
          accountingAdjustment = 0)
        .create()

      calculations.SelfEmployment.TaxableProfit(selfEmployment) shouldBe BigDecimal(5000.00)
    }
  }

  "Incomes" should {
    "be profit per self employment source" in {
      val dummyID = BSONObjectID.generate

      val selfEmploymentOne = SelfEmploymentBuilder(dummyID)
        .withTurnover(2000)
        .create()
      val selfEmploymentTwo = SelfEmploymentBuilder(dummyID)
        .withTurnover(3000)
        .create()

      calculations.SelfEmployment.Incomes(SelfAssessment(selfEmployments = Seq(selfEmploymentOne, selfEmploymentTwo))) should contain theSameElementsAs
        Seq(SelfEmploymentIncome(dummyID.stringify, profit = 2000, taxableProfit = 2000),
          SelfEmploymentIncome(dummyID.stringify, profit = 3000, taxableProfit = 3000))
    }
  }

  "TotalLossBroughtForward" should {
    "be the Rounded up sum of all loss brought forward" in {
      val selfEmploymentOne = SelfEmploymentBuilder()
        .withTurnover(2000)
        .withAdjustments(
          lossBroughtForward = 100.45,
          outstandingBusinessIncome = 0,
          averagingAdjustment = 0,
          overlapReliefUsed = 0,
          basisAdjustment = 0,
          includedNonTaxableProfits = 0,
          accountingAdjustment = 0)
        .create()

      val selfEmploymentTwo = SelfEmploymentBuilder()
        .withTurnover(2000)
        .withAdjustments(
          lossBroughtForward = 200.13,
          outstandingBusinessIncome = 0,
          averagingAdjustment = 0,
          overlapReliefUsed = 0,
          basisAdjustment = 0,
          includedNonTaxableProfits = 0,
          accountingAdjustment = 0)
        .create()

      calculations.SelfEmployment.TotalLossBroughtForward(SelfAssessment(selfEmployments = Seq(selfEmploymentOne, selfEmploymentTwo))) shouldBe 301
    }

    "be the Rounded up sum of all loss brought forward capped at adjusted profits" in {
      val selfEmploymentOne = SelfEmploymentBuilder()
        .withTurnover(2000.45)
        .withAdjustments(
          lossBroughtForward = 2100,
          outstandingBusinessIncome = 0,
          averagingAdjustment = 0,
          overlapReliefUsed = 0,
          basisAdjustment = 0,
          includedNonTaxableProfits = 0,
          accountingAdjustment = 0)
        .create()

      val selfEmploymentTwo = SelfEmploymentBuilder()
        .withTurnover(2000)
        .withAdjustments(
          lossBroughtForward = 1000.45,
          outstandingBusinessIncome = 0,
          averagingAdjustment = 0,
          overlapReliefUsed = 0,
          basisAdjustment = 0,
          includedNonTaxableProfits = 0,
          accountingAdjustment = 0)
        .create()

      calculations.SelfEmployment.TotalLossBroughtForward(SelfAssessment(selfEmployments = Seq(selfEmploymentOne, selfEmploymentTwo))) shouldBe 3001
    }
  }

}
