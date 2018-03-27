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

  val genExpenses: Gen[Expenses] =
    for {
      costOfGoodsBought <- Gen.option(genExpense())
      cisPaymentsToSubcontractors <- Gen.option(genExpense())
      staffCosts <- Gen.option(genExpense())
      travelCosts <- Gen.option(genExpense())
      premisesRunningCosts <- Gen.option(genExpense())
      maintenanceCosts <- Gen.option(genExpense())
      adminCosts <- Gen.option(genExpense())
      advertisingCosts <- Gen.option(genExpense())
      interest <- Gen.option(genExpense())
      financialCharges <- Gen.option(genExpense())
      badDebt <- Gen.option(genExpense())
      professionalFees <- Gen.option(genExpense())
      depreciation <- Gen.option(genExpense(depreciation = true))
      other <- Gen.option(genExpense())
    } yield
      Expenses(costOfGoodsBought = costOfGoodsBought,
               cisPaymentsToSubcontractors = cisPaymentsToSubcontractors,
               staffCosts = staffCosts,
               travelCosts = travelCosts,
               premisesRunningCosts = premisesRunningCosts,
               maintenanceCosts = maintenanceCosts,
               adminCosts = adminCosts,
               advertisingCosts = advertisingCosts,
               interest = interest,
               financialCharges = financialCharges,
               badDebt = badDebt,
               professionalFees = professionalFees,
               depreciation = depreciation,
               other = other)

}
