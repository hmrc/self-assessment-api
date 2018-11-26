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

package uk.gov.hmrc.selfassessmentapi.fixtures.properties

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.models.properties.{Loss, Profit, PropertiesBISS, Total}

object PropertiesBISSFixture {

  def propertiesBISS() = {
    PropertiesBISS(
      Total(
        income = 10.50,
        expenses = 10.50,
        additions = Some(10.50),
        deductions = Some(10.50)
      ),
      Profit(
        net = 10.50,
        taxable = 10.50
      ),
      Loss(
        net = 10.50,
        taxable = 10.50
      )
    )
  }

  def propertiesBISSDESJson() = {
    Json.obj(
      "totalIncome" -> 10.50,
      "totalExpenses" -> 10.50,
      "totalAdditions" -> 10.50,
      "totalDeductions" -> 10.50,
      "netProfit" -> 10.50,
      "netLoss" -> 10.50,
      "taxableProfit" -> 10.50,
      "taxableLoss" -> 10.50
    )
  }
}
