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

import uk.gov.hmrc.selfassessmentapi.controllers.api.{SelfEmploymentIncome, SelfAssessment}
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.ExpenseType
import uk.gov.hmrc.selfassessmentapi.controllers.api.{CapAt, RoundDown, _}
import uk.gov.hmrc.selfassessmentapi.repositories._

object SelfEmployment {

  object TotalTaxableProfit {
    def apply(selfAssessment: SelfAssessment): BigDecimal =
      apply(TotalProfit(selfAssessment), Deductions.Total(selfAssessment))
    def apply(totalProfit: BigDecimal, totalDeduction: BigDecimal): BigDecimal =
      PositiveOrZero(totalProfit - totalDeduction)
  }

  object TotalProfit {
    def apply(selfAssessment: SelfAssessment) = selfAssessment.selfEmployments.map(Profit(_)).sum
  }

  object AdjustedProfit {

    private val annualInvestmentAllowanceThreshold = 200000

    def apply(selfEmployment: domain.SelfEmployment) = {
      val profitIncreases = {
        val adjustments =
          selfEmployment.adjustments.map(a => Sum(a.basisAdjustment, a.accountingAdjustment, a.averagingAdjustment))
        Total(selfEmployment.incomes) + Total(selfEmployment.balancingCharges) + Total(
          selfEmployment.goodsAndServicesOwnUse) +
          adjustments.getOrElse(0)
      }

      val profitReductions = {
        val adjustments = selfEmployment.adjustments.map { a =>
          Sum(a.includedNonTaxableProfits, a.overlapReliefUsed)
        }
        selfEmployment.expenses.map(expense => expense.amount - expense.disallowableAmount).sum + totalAllowances(
          selfEmployment) + adjustments.getOrElse(0)
      }

      PositiveOrZero(profitIncreases - profitReductions)
    }

    def totalAllowances(selfEmployment: domain.SelfEmployment) = {
      selfEmployment.allowances.map { a =>
        Sum(CapAt(a.annualInvestmentAllowance, annualInvestmentAllowanceThreshold),
            a.capitalAllowanceMainPool,
            a.capitalAllowanceSpecialRatePool,
            a.businessPremisesRenovationAllowance,
            a.enhancedCapitalAllowance,
            a.allowanceOnSales)
      }.sum
    }
  }

  object Profit {
    def apply(selfEmployment: domain.SelfEmployment) = {
      RoundDown(AdjustedProfit(selfEmployment) + selfEmployment.outstandingBusinessIncome)
    }
  }

  object Incomes {
    def apply(selfAssessment: SelfAssessment) = selfAssessment.selfEmployments.map { selfEmployment =>
      SelfEmploymentIncome(selfEmployment.sourceId,
                           profit = RoundDown(Profit(selfEmployment)),
                           taxableProfit = TaxableProfit(selfEmployment))
    }
  }

  object TaxableProfit {
    def apply(selfEmployment: domain.SelfEmployment): BigDecimal =
      RoundDown(
        PositiveOrZero(
          Profit(selfEmployment) - CapAt(LossBroughtForward(selfEmployment), AdjustedProfit(selfEmployment))))
  }

  object LossBroughtForward {
    def apply(selfEmployment: domain.SelfEmployment) =
      ValueOrZero(selfEmployment.adjustments.flatMap(_.lossBroughtForward))
  }

  object TotalLossBroughtForward {
    def apply(selfAssessment: SelfAssessment): BigDecimal = {
      RoundUp(selfAssessment.selfEmployments.map { selfEmployment =>
        CapAt(selfEmployment.lossBroughtForward, selfEmployment.adjustedProfits)
      }.sum)
    }
  }

}
