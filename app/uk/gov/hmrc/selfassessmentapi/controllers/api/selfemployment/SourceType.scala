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
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SummaryTypes.{BalancingCharges, Expenses, GoodsAndServicesOwnUses, Incomes}

object SourceType {

  case object SelfEmployments extends SourceType {
    override val name = "self-employments"
    override val documentationName = "Self Employments"

    override def example(sourceId: Option[SourceId] = None): JsValue = toJson(SelfEmployment.example(sourceId))

    override val summaryTypes: Set[SummaryType] = Set(Incomes, Expenses, GoodsAndServicesOwnUses, BalancingCharges)
    override val title = "Sample self-employments"

    override def description(action: String) = s"$action a self-employment"

    override val fieldDescriptions = Seq(
      FullFieldDescription(name, "commencementDate", "Date", "Date in yyyy-dd-mm format"),
      FullFieldDescription(name, "allowances", "Object", "Allowances claimed for this self-employment", optional = true),
      PositiveMonetaryFieldDescription(name, "annualInvestmentAllowance", "Annual Investment Allowance of up to £200,000 can be claimed for purchases of equipment (but not cars) on or after 6 April 2014", optional = true),
      PositiveMonetaryFieldDescription(name, "capitalAllowanceMainPool",
        """Writing down allowance of 18% can be claimed on the final balance of main pool costs.
          |If the final balance before claiming WDA is £1,000 or less, a small pool allowance can be claimed for the full amount instead of the WDA""".stripMargin, optional = true),
      PositiveMonetaryFieldDescription(name, "capitalAllowanceSpecialRatePool",
        """Writing down allowance of 8% can be claimed on the final balance of the special rate pool costs.
          |If the final balance before claiming WDA is £1,000 or less, a small pool allowance can be claimed for the full amount instead of the WDA""".stripMargin, optional = true),
      PositiveMonetaryFieldDescription(name, "businessPremisesRenovationAllowance", "When eligible, BPRA can be claimed for the cost of renovating or repairing unused business premises", optional = true),
      PositiveMonetaryFieldDescription(name, "enhancedCapitalAllowance", "100% capital allowance can be claimed for eligible capital purchases", optional = true),
      PositiveMonetaryFieldDescription(name, "allowancesOnSales",
        """If the business ceases, any balance left in the relevant pool can be claimed after
          |either the selling price or market value has been deducted from the pool balance, as a balancing allowance instead of claiming a WDA""".stripMargin, optional = true)
      )
  }

}
