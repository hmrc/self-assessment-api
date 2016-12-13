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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.builders

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.TestUtils._
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.BalancingChargeType.BalancingChargeType
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.IncomeType.IncomeType
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.{SelfEmployment => _, _}
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.repositories.domain._
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.{Adjustments, Allowances}

case class SelfEmploymentBuilder(objectID: BSONObjectID = BSONObjectID.generate) {
  def create() = selfEmployment

  private var selfEmployment: SelfEmployment =
    SelfEmployment(id = objectID,
                   sourceId = objectID.stringify,
                   NinoGenerator().nextNino(),
                   taxYear,
                   now,
                   now,
                   now.toLocalDate,
                   Some(Allowances()),
                   Some(Adjustments()))

  def withAllowances(allowancesOnSales: BigDecimal,
                     enhancedCapitalAllowance: BigDecimal,
                     businessPremisesRenovationAllowance: BigDecimal,
                     capitalAllowanceSpecialRatePool: BigDecimal,
                     capitalAllowanceMainPool: BigDecimal,
                     annualInvestmentAllowance: BigDecimal) = {
    selfEmployment = selfEmployment.copy(
      allowances = selfEmployment.allowances.map(
        _.copy(allowanceOnSales = Some(allowancesOnSales),
               enhancedCapitalAllowance = Some(enhancedCapitalAllowance),
               businessPremisesRenovationAllowance = Some(businessPremisesRenovationAllowance),
               capitalAllowanceSpecialRatePool = Some(capitalAllowanceSpecialRatePool),
               capitalAllowanceMainPool = Some(capitalAllowanceMainPool),
               annualInvestmentAllowance = Some(annualInvestmentAllowance))))

    this
  }

  def withAdjustments(outstandingBusinessIncome: BigDecimal = 0,
                      lossBroughtForward: BigDecimal = 0,
                      averagingAdjustment: BigDecimal = 0,
                      overlapReliefUsed: BigDecimal = 0,
                      basisAdjustment: BigDecimal = 0,
                      includedNonTaxableProfits: BigDecimal = 0,
                      accountingAdjustment: BigDecimal = 0) = {
    selfEmployment = selfEmployment.copy(
      adjustments = selfEmployment.adjustments.map(
        _.copy(outstandingBusinessIncome = Some(outstandingBusinessIncome),
               lossBroughtForward = Some(lossBroughtForward),
               averagingAdjustment = Some(averagingAdjustment),
               overlapReliefUsed = Some(overlapReliefUsed),
               basisAdjustment = Some(basisAdjustment),
               includedNonTaxableProfits = Some(includedNonTaxableProfits),
               accountingAdjustment = Some(accountingAdjustment))))

    this
  }

  private def incomes(incomes: (IncomeType, BigDecimal)*) = {
    selfEmployment = selfEmployment.copy(incomes = selfEmployment.incomes ++ incomes.map(income =>
        SelfEmploymentIncomeSummary(objectID.stringify, income._1, income._2)))
    this
  }

  def withTurnover(turnovers: BigDecimal*) = {
    incomes(turnovers.map((IncomeType.Turnover, _)): _*)
  }

  def withOtherIncome(otherIncomes: BigDecimal*) = {
    incomes(otherIncomes.map((IncomeType.Other, _)): _*)
  }

  private def expenses(expenses: Seq[Expense]) = {
    selfEmployment = selfEmployment.copy(
      expenses = selfEmployment.expenses ++ expenses.map(SelfEmploymentExpenseSummary.toMongoSummary(_)))
    this
  }

  def withPremisesRunningCosts(costs: (BigDecimal, BigDecimal)*) =
    expenses(costs.map {
      case (amount, disallowableAmount) =>
        Expense(`type` = ExpenseType.PremisesRunningCosts, amount = amount, disallowableAmount = disallowableAmount)
    })

  def withAdminCosts(adminCosts: (BigDecimal, BigDecimal)*) =
    expenses(adminCosts.map {
      case (amount, disallowableAmount) =>
        Expense(`type` = ExpenseType.AdminCosts, amount = amount, disallowableAmount = disallowableAmount)
    })

  def withBadDebt(badDebts: (BigDecimal, BigDecimal)*) =
    expenses(badDebts.map {
      case (amount, disallowableAmount) =>
        Expense(`type` = ExpenseType.BadDebt, amount = amount, disallowableAmount = disallowableAmount)
    })

  def withCISPaymentsToSubcontractors(paymentsToSubcontractors: (BigDecimal, BigDecimal)*) =
    expenses(paymentsToSubcontractors.map {
      case (amount, disallowableAmount) =>
        Expense(`type` = ExpenseType.CISPaymentsToSubcontractors,
                amount = amount,
                disallowableAmount = disallowableAmount)
    })

  def withDepreciation(deprecations: (BigDecimal, BigDecimal)*) =
    expenses(deprecations.map {
      case (amount, disallowableAmount) =>
        Expense(`type` = ExpenseType.Depreciation, amount = amount, disallowableAmount = disallowableAmount)
    })

  private def balancingCharges(balancingCharges: (BalancingChargeType, BigDecimal)*) = {
    selfEmployment =
      selfEmployment.copy(balancingCharges = selfEmployment.balancingCharges ++ balancingCharges.map(balancingCharge =>
          SelfEmploymentBalancingChargeSummary(objectID.stringify, balancingCharge._1, balancingCharge._2)))
    this
  }

  def withBpraBalancingCharges(charges: BigDecimal*) = {
    balancingCharges(charges.map((BalancingChargeType.BPRA, _)): _*)
  }

  def withOtherBalancingCharges(charges: BigDecimal*) = {
    balancingCharges(charges.map((BalancingChargeType.Other, _)): _*)
  }

  def goodsAndServicesOwnUse(amounts: BigDecimal*) = {
    selfEmployment = selfEmployment.copy(
      goodsAndServicesOwnUse = amounts.map(SelfEmploymentGoodsAndServicesOwnUseSummary(objectID.stringify, _)))
    this
  }
}
