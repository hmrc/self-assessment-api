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

package uk.gov.hmrc.selfassessmentapi.models.des.selfemployment

import org.joda.time.LocalDate
import uk.gov.hmrc.selfassessmentapi.models
import uk.gov.hmrc.selfassessmentapi.models.{Expense, SimpleIncome}
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class SelfEmploymentPeriodSpec extends JsonSpec {
  "from" should {
    "correctly map an API self-employment period to a DES self-employment period" in {
      val apiPeriod = models.selfemployment.SelfEmploymentPeriod(
        id = Some("abc"),
        from = LocalDate.parse("2017-04-06"),
        to = LocalDate.parse("2018-04-05"),
        incomes =
          Some(models.selfemployment.Incomes(turnover = Some(SimpleIncome(10.10)), other = Some(SimpleIncome(10.10)))),
        expenses = Some(
          models.selfemployment.Expenses(
            cisPaymentsToSubcontractors = Some(Expense(10.10, Some(10.10))),
            depreciation = Some(Expense(10.10, Some(10.10))),
            costOfGoodsBought = Some(Expense(10.10, Some(10.10))),
            professionalFees = Some(Expense(10.10, Some(10.10))),
            badDebt = Some(Expense(10.10, Some(10.10))),
            adminCosts = Some(Expense(10.10, Some(10.10))),
            advertisingCosts = Some(Expense(10.10, Some(10.10))),
            financialCharges = Some(Expense(10.10, Some(10.10))),
            interest = Some(Expense(10.10, Some(10.10))),
            maintenanceCosts = Some(Expense(10.10, Some(10.10))),
            premisesRunningCosts = Some(Expense(10.10, Some(10.10))),
            staffCosts = Some(Expense(10.10, Some(10.10))),
            travelCosts = Some(Expense(10.10, Some(10.10))),
            other = Some(Expense(10.10, Some(10.10)))
          )),
          consolidatedExpenses = None)

      val desPeriod = SelfEmploymentPeriod.from(apiPeriod)

      (SelfEmploymentPeriod.from _ compose
        models.selfemployment.SelfEmploymentPeriod.from)(desPeriod) shouldBe desPeriod

    }
  }
}
