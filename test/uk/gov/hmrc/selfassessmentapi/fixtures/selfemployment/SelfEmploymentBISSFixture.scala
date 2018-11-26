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

package uk.gov.hmrc.selfassessmentapi.fixtures.selfemployment

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.{Loss, Profit, SelfEmploymentBISS, Total}

object SelfEmploymentBISSFixture {

  def selfEmploymentBISS = {
    SelfEmploymentBISS(
      Total(
        income = 10.55,
        expenses = 10.55,
        additions = Some(10.55),
        deductions = Some(10.55)
      ),
      accountingAdjustments = Some(10.55),
      Profit(
        net = 10.55,
        taxable = 10.55
      ),
      Loss(
        net = 10.55,
        taxable = 10.55
      )
    )
  }

  def selfEmploymentBISSJson = {
    Json.obj(
      "total" -> Json.obj(
        "income" -> 10.55,
        "expenses" -> 10.55,
        "additions" -> 10.55,
        "deductions" -> 10.55
      ),
      "profit" -> Json.obj(
        "net" -> 10.55,
        "taxable" -> 10.55
      ),
      "loss" -> Json.obj(
        "net" -> 10.55,
        "taxable" -> 10.55
      ),
      "accountingAdjustments" -> 10.55
    )
  }

  object Des {
    def selfEmploymentBISSJson = {
      Json.obj(
        "totalIncome" -> 10.55,
        "totalExpenses" -> 10.55,
        "totalAdditions" -> 10.55,
        "totalDeductions" -> 10.55,
        "accountingAdjustments" -> 10.55,
        "netProfit" -> 10.55,
        "netLoss" -> 10.55,
        "taxableProfit" -> 10.55,
        "taxableLoss" -> 10.55
      )
    }
  }
}
