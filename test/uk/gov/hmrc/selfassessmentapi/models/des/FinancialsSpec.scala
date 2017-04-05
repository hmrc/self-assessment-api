/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.des

import uk.gov.hmrc.selfassessmentapi.models
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.{ExpenseType, IncomeType}
import uk.gov.hmrc.selfassessmentapi.models.{Expense, SimpleIncome}
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class FinancialsSpec extends JsonSpec {
  "from" should {
    "correctly map a API self-employment update into a DES self-employment update" in {
      val apiUpdate = models.selfemployment.SelfEmploymentPeriodicData(
        incomes = Map(
          IncomeType.Turnover -> SimpleIncome(10.10),
          IncomeType.Other -> SimpleIncome(10.10)),
        expenses = Map(
          ExpenseType.CISPaymentsToSubcontractors -> Expense(10.10, Some(10.10)),
          ExpenseType.Depreciation -> Expense(10.10, Some(10.10)),
          ExpenseType.CostOfGoodsBought -> Expense(10.10, Some(10.10)),
          ExpenseType.ProfessionalFees -> Expense(10.10, Some(10.10)),
          ExpenseType.BadDebt -> Expense(10.10, Some(10.10)),
          ExpenseType.AdminCosts -> Expense(10.10, Some(10.10)),
          ExpenseType.AdvertisingCosts -> Expense(10.10, Some(10.10)),
          ExpenseType.FinancialCharges -> Expense(10.10, Some(10.10)),
          ExpenseType.Interest -> Expense(10.10, Some(10.10)),
          ExpenseType.MaintenanceCosts -> Expense(10.10, Some(10.10)),
          ExpenseType.PremisesRunningCosts -> Expense(10.10, Some(10.10)),
          ExpenseType.StaffCosts -> Expense(10.10, Some(10.10)),
          ExpenseType.TravelCosts -> Expense(10.10, Some(10.10)),
          ExpenseType.Other -> Expense(10.10, Some(10.10))))

      val desUpdate = Financials.from(apiUpdate)
      val desIncomes = desUpdate.incomes.get
      val desDeductions = desUpdate.deductions.get

      desIncomes.turnover shouldBe Some(10.10)
      desIncomes.other shouldBe Some(10.10)

      desDeductions.constructionIndustryScheme shouldBe Some(Deduction(10.10, Some(10.10)))
      desDeductions.depreciation shouldBe Some(Deduction(10.10, Some(10.10)))
      desDeductions.costOfGoods shouldBe Some(Deduction(10.10, Some(10.10)))
      desDeductions.professionalFees shouldBe Some(Deduction(10.10, Some(10.10)))
      desDeductions.badDebt shouldBe Some(Deduction(10.10, Some(10.10)))
      desDeductions.adminCosts shouldBe Some(Deduction(10.10, Some(10.10)))
      desDeductions.advertisingCosts shouldBe Some(Deduction(10.10, Some(10.10)))
      desDeductions.financialCharges shouldBe Some(Deduction(10.10, Some(10.10)))
      desDeductions.interest shouldBe Some(Deduction(10.10, Some(10.10)))
      desDeductions.maintenanceCosts shouldBe Some(Deduction(10.10, Some(10.10)))
      desDeductions.premisesRunningCosts shouldBe Some(Deduction(10.10, Some(10.10)))
      desDeductions.staffCosts shouldBe Some(Deduction(10.10, Some(10.10)))
      desDeductions.travelCosts shouldBe Some(Deduction(10.10, Some(10.10)))
      desDeductions.other shouldBe Some(Deduction(10.10, Some(10.10)))
    }
  }
}
