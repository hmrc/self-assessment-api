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

package uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SummaryType, FullFieldDescription, PositiveMonetaryFieldDescription}
import uk.gov.hmrc.selfassessmentapi.controllers.api._

object SummaryTypes {

  case object Incomes extends SummaryType {
    override val name = "incomes"
    override val documentationName = "Incomes"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(Income.example(id))
    override val title = "Sample self-employment incomes"
    override def description(action: String) = s"$action an income for the specified source"
    override val fieldDescriptions = Seq(
      FullFieldDescription("self-employments", "type", "Enum", s"Type of income (one of the following: ${IncomeType.values.mkString(", ")})"),
      PositiveMonetaryFieldDescription("self-employments", "amount", "Income from the business including turnover (from takings, fees & sales) earned or received by the business before expenses, and other business income not included within turnover")
    )
  }

  case object Expenses extends SummaryType {
    override val name = "expenses"
    override val documentationName = "Expenses"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(Expense.example(id))
    override val title = "Sample self-employment expenses"
    override def description(action: String) = s"$action an expense for the specified source"
    override val fieldDescriptions = Seq(
      FullFieldDescription("self-employments", "type", "Enum", s"Type of expense (one of the following: ${ExpenseType.values.mkString(", ")})"),
      PositiveMonetaryFieldDescription("self-employments", "amount", "Expenses associated with the running of the business, split by expense type"),
      PositiveMonetaryFieldDescription("self-employments", "disallowableAmount", "Any expense or partial expense that cannot be claimed for tax purposes")
    )
  }

  case object BalancingCharges extends SummaryType {
    override val name = "balancing-charges"
    override val documentationName = "Balancing Charges"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(BalancingCharge.example(id))
    override val title = "Sample self-employment balancing charges"
    override def description(action: String) = s"$action a balancing charge for the specified source"
    override val fieldDescriptions = Seq(
      FullFieldDescription("self-employments", "type", "Enum", s"Type of balancing charge (one of the following: ${BalancingChargeType.values.mkString(", ")})"),
      PositiveMonetaryFieldDescription("self-employments", "amount", "If an item has been sold where capital allowance was claimed, and the sale or value of the item is more than the pool value or cost, " +
        "a balancing charge is required to pay tax on the difference")
    )
  }

  case object GoodsAndServicesOwnUses extends SummaryType {
    override val name = "goods-and-services-own-uses"
    override val documentationName = "Goods and Services Own Uses"
    override def example(id: Option[SummaryId] = None): JsValue = toJson(GoodsAndServicesOwnUse.example(id))
    override val title = "Sample self-employment goods and service for own use"
    override def description(action: String) = s"$action a goods and service for own use summary for the specified source"
    override val fieldDescriptions = Seq(
      PositiveMonetaryFieldDescription("self-employments", "amount", "To account for the normal sale price of goods or stock have been taken out of the business")
    )
  }
}
