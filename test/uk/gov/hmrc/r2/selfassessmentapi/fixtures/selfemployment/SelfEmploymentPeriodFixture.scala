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

import org.joda.time.LocalDate
import uk.gov.hmrc.selfassessmentapi.models
import uk.gov.hmrc.selfassessmentapi.models.{Expense, SimpleIncome, ExpenseNegativeOrPositive, ExpenseProfessionalFees}

object SelfEmploymentPeriodFixture {

  def period(sourceId: Option[String] = Some("abc"),
            fromDate: LocalDate = LocalDate.parse("2017-04-06"),
             toDate: LocalDate = LocalDate.parse("2018-04-05")) = {
    models.selfemployment.SelfEmploymentPeriod(
      id = sourceId,
      from = fromDate,
      to = toDate,
      Some(models.selfemployment.Incomes(
        turnover = Some(SimpleIncome(10.10)),
        other = Some(SimpleIncome(10.10)))),
      Some(models.selfemployment.Expenses(
        cisPaymentsToSubcontractors = Some(Expense(10.10, Some(10.10))),
        depreciation = Some(ExpenseNegativeOrPositive(10.10, Some(10.10))),
        costOfGoodsBought = Some(ExpenseNegativeOrPositive(10.10, Some(10.10))),
        professionalFees = Some(ExpenseProfessionalFees(10.10, Some(10.10))),
        badDebt = Some(Expense(10.10, Some(10.10))),
        adminCosts = Some(Expense(10.10, Some(10.10))),
        advertisingCosts = Some(Expense(10.10, Some(10.10))),
        businessEntertainmentCosts = Some(Expense(10.10, Some(10.10))),
        financialCharges = Some(ExpenseNegativeOrPositive(10.10, Some(10.10))),
        interest = Some(ExpenseNegativeOrPositive(10.10, Some(10.10))),
        maintenanceCosts = Some(ExpenseNegativeOrPositive(10.10, Some(10.10))),
        premisesRunningCosts = Some(ExpenseNegativeOrPositive(10.10, Some(10.10))),
        staffCosts = Some(Expense(10.10, Some(10.10))),
        travelCosts = Some(Expense(10.10, Some(10.10))),
        other = Some(Expense(10.10, Some(10.10)))
      )),
      consolidatedExpenses = None)
  }
}
