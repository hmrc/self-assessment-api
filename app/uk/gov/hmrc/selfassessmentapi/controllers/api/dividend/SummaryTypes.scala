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

package uk.gov.hmrc.selfassessmentapi.controllers.api.dividend

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{FullFieldDescription, PositiveMonetaryFieldDescription, SummaryType, _}

object SummaryTypes {

  case object Incomes extends SummaryType {
    override val name = "incomes"
    override val documentationName = "Incomes"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(DividendIncome.example(id))
    override val title = "Sample dividend incomes"
    override def description(action: String) = s"$action an income for the specified source"
    override val fieldDescriptions = Seq(
      FullFieldDescription("dividends", "type", "Enum", s"Type of income (one of the following: ${DividendIncomeType.values.mkString(", ")})"),
      PositiveMonetaryFieldDescription("dividends", "amount", "Income from the business including turnover (from takings, fees & sales) earned or received by the business before expenses, and other business income not included within turnover.")
    )
  }
}
