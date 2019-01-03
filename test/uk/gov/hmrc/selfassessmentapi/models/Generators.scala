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

package uk.gov.hmrc.selfassessmentapi.models

import org.scalacheck.Gen
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.{Expenses, Incomes}

object Generators {
  def amountGen(lower: BigDecimal, upper: BigDecimal): Gen[BigDecimal] =
    for (value <- Gen.chooseNum(lower.intValue(), upper.intValue())) yield BigDecimal(value)

  val amount: Gen[BigDecimal] = amountGen(1000, 5000)

  val genSimpleIncome: Gen[SimpleIncome] = for (amount <- amount) yield SimpleIncome(amount)

  val genIncomes: Gen[Incomes] =
    for {
      turnover <- Gen.option(genSimpleIncome)
      other <- Gen.option(genSimpleIncome)
    } yield Incomes(turnover = turnover, other = other)

  def genExpense(depreciation: Boolean = false): Gen[Expense] =
    for {
      amount <- amount
      disallowableAmount <- Gen.option(amountGen(0, amount))
    } yield Expense(amount = amount, disallowableAmount = if (depreciation) Some(amount) else disallowableAmount)

  def genExpenseNegativeOrPositive(depreciation: Boolean = false): Gen[ExpenseNegativeOrPositive] =
    for {
      amount <- amount
      disallowableAmount <- Gen.option(amountGen(0, amount))
    } yield ExpenseNegativeOrPositive(amount = amount, disallowableAmount = if (depreciation) Some(amount) else disallowableAmount)

  def genExpenseProfessionalFees(depreciation: Boolean = false): Gen[ExpenseProfessionalFees] =
    for {
      amount <- amount
      disallowableAmount <- Gen.option(amountGen(0, amount))
    } yield ExpenseProfessionalFees(amount = amount, disallowableAmount = if (depreciation) Some(amount) else disallowableAmount)

  val genExpenses: Gen[Expenses] =
    for {
      costOfGoodsBought <- Gen.option(genExpenseNegativeOrPositive())
      cisPaymentsToSubcontractors <- Gen.option(genExpense())
      staffCosts <- Gen.option(genExpense())
      travelCosts <- Gen.option(genExpense())
      premisesRunningCosts <- Gen.option(genExpenseNegativeOrPositive())
      maintenanceCosts <- Gen.option(genExpenseNegativeOrPositive())
      adminCosts <- Gen.option(genExpense())
      advertisingCosts <- Gen.option(genExpense())
      interest <- Gen.option(genExpenseNegativeOrPositive())
      financialCharges <- Gen.option(genExpenseNegativeOrPositive())
      badDebt <- Gen.option(genExpense())
      professionalFees <- Gen.option(genExpenseProfessionalFees())
      depreciation <- Gen.option(genExpenseNegativeOrPositive(depreciation = true))
      other <- Gen.option(genExpense())
    } yield
      Expenses(costOfGoodsBought,
               cisPaymentsToSubcontractors,
               staffCosts,
               travelCosts,
               premisesRunningCosts,
               maintenanceCosts,
               adminCosts,
               advertisingCosts,
               None,
               interest,
               financialCharges,
               badDebt,
               professionalFees,
               depreciation ,
               other)

}
