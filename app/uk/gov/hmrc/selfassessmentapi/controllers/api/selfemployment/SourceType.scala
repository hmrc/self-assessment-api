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
          |either the selling price or market value has been deducted from the pool balance, as a balancing allowance instead of claiming a WDA""".stripMargin, optional = true),
      FullFieldDescription(name, "adjustments", "Object", "Adjustments for this self-employment", optional = true),
      PositiveMonetaryFieldDescription(name, "includedNonTaxableProfits", "For income, receipts and other profits that have been included in business turnover but are not taxable as business profits", optional = true),
      MonetaryFieldDescription(name, "basisAdjustment",
        """Tax is paid on the profits of the basis period for the tax year.
          |If the basis period is not the same as the accounting period, an adjustment is needed to arrive at the profit or loss for the basis period""".stripMargin, optional = true),
      PositiveMonetaryFieldDescription(name, "overlapReliefUsed", "When eligible, overlap relief can claimed if the business has overlap profits", optional = true),
      PositiveMonetaryFieldDescription(name, "accountingAdjustment", "If accounting practice has changed (from cash to accrual) an adjustment may be required", optional = true),
      MonetaryFieldDescription(name, "averagingAdjustment", "If an averaging claim changes the amount of the profit, an adjustment is required. Cannot be used if using cash basis", optional = true),
      PositiveMonetaryFieldDescription(name, "lossBroughtForward",
        """If a loss was made in the previous or earlier tax years, this can be used against the profits from this tax year.
           The loss claimed cannot be more than the adjusted profit for this tax year
        """.stripMargin, optional = true),
      PositiveMonetaryFieldDescription(name, "outstandingBusinessIncome", """For other business income that hasn’t been included such as rebates received and non arm’s length reverse premiums""", optional = true)
    )
  }

}
