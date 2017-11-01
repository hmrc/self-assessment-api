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
import uk.gov.hmrc.selfassessmentapi.models.des.selfemployment.{Deduction, Financials}
import uk.gov.hmrc.selfassessmentapi.models.{Expense, SimpleIncome}
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class FinancialsSpec extends JsonSpec {
  "from" should {
    "correctly map a API self-employment update into a DES self-employment update" in {
      val apiUpdate = models.selfemployment.SelfEmploymentPeriodUpdate(
        incomes =
          Some(models.selfemployment.Incomes(turnover = Some(SimpleIncome(10.10)), other = Some(SimpleIncome(10.10)))),
        expenses = Some(
          models.selfemployment.Expenses(
            cisPaymentsToSubcontractors = Some(10.10),
            depreciation = Some(10.10),
            costOfGoodsBought = Some(10.10),
            professionalFees = Some(10.10),
            badDebt = Some(10.10),
            adminCosts = Some(10.10),
            advertisingCosts = Some(10.10),
            financialCharges = Some(10.10),
            interest = Some(10.10),
            maintenanceCosts = Some(10.10),
            premisesRunningCosts = Some(10.10),
            staffCosts = Some(10.10),
            travelCosts = Some(10.10),
            other = Some(10.10)
          )),
        consolidatedExpenses = None)

      val desUpdate = Financials.from(apiUpdate)
      val desIncomes = desUpdate.incomes.get
      val desDeductions = desUpdate.deductions.get

      desIncomes.turnover shouldBe Some(10.10)
      desIncomes.other shouldBe Some(10.10)

      desDeductions.constructionIndustryScheme shouldBe Some(Deduction(10.10, None))
      desDeductions.depreciation shouldBe Some(Deduction(10.10, None))
      desDeductions.costOfGoods shouldBe Some(Deduction(10.10, None))
      desDeductions.professionalFees shouldBe Some(Deduction(10.10, None))
      desDeductions.badDebt shouldBe Some(Deduction(10.10, None))
      desDeductions.adminCosts shouldBe Some(Deduction(10.10, None))
      desDeductions.advertisingCosts shouldBe Some(Deduction(10.10, None))
      desDeductions.financialCharges shouldBe Some(Deduction(10.10, None))
      desDeductions.interest shouldBe Some(Deduction(10.10, None))
      desDeductions.maintenanceCosts shouldBe Some(Deduction(10.10, None))
      desDeductions.premisesRunningCosts shouldBe Some(Deduction(10.10, None))
      desDeductions.staffCosts shouldBe Some(Deduction(10.10, None))
      desDeductions.travelCosts shouldBe Some(Deduction(10.10, None))
      desDeductions.other shouldBe Some(Deduction(10.10, None))
    }
  }
}
