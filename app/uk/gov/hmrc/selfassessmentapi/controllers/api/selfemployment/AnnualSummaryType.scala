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

object AnnualSummaryType {
  case object Adjustments extends AnnualSummaryType {
    override val name: String = "adjustments"
    override val documentationName: String = "Adjustments"
    override def example(id: Option[SummaryId]): JsValue = toJson(selfemployment.Adjustments.example(None))
    override val title: String = "Sample self-employment adjustment"
    override def description(action: String): String = s"$action a adjustment for the specified source"
    override val fieldDescriptions = Seq(
      PositiveMonetaryFieldDescription("self-employments", "includedNonTaxableProfits", "For income, receipts and other profits that have been included in business turnover but are not taxable as business profits", optional = true),
      MonetaryFieldDescription("self-employments", "basisAdjustment",
        """Tax is paid on the profits of the basis period for the tax year.
          |If the basis period is not the same as the accounting period, an adjustment is needed to arrive at the profit or loss for the basis period""".stripMargin, optional = true),
      PositiveMonetaryFieldDescription("self-employments", "overlapReliefUsed", "When eligible, overlap relief can claimed if the business has overlap profits", optional = true),
      PositiveMonetaryFieldDescription("self-employments", "accountingAdjustment", "If accounting practice has changed (from cash to accrual) an adjustment may be required", optional = true),
      MonetaryFieldDescription("self-employments", "averagingAdjustment", "If an averaging claim changes the amount of the profit, an adjustment is required. Cannot be used if using cash basis", optional = true),
      PositiveMonetaryFieldDescription("self-employments", "lossBroughtForward",
        """If a loss was made in the previous or earlier tax years, this can be used against the profits from this tax year.
           The loss claimed cannot be more than the adjusted profit for this tax year
        """.stripMargin, optional = true),
      PositiveMonetaryFieldDescription("self-employments", "outstandingBusinessIncome", """For other business income that hasn’t been included such as rebates received and non arm’s length reverse premiums""", optional = true)
    )
  }

  case object Allowances extends AnnualSummaryType {
    override val name: String = "allowances"
    override val documentationName: String = "Allowances"
    override def example(id: Option[SummaryId]): JsValue = toJson(selfemployment.Allowances.example(None))
    override val title: String = "Sample self-employment allowance"
    override def description(action: String): String = s"$action a allowance for the specified source"
    override val fieldDescriptions = Seq(
      PositiveMonetaryFieldDescription("self-employments", "annualInvestmentAllowance", "Annual Investment Allowance of up to £200,000 can be claimed for purchases of equipment (but not cars) on or after 6 April 2014", optional = true),
      PositiveMonetaryFieldDescription("self-employments", "capitalAllowanceMainPool",
        """Writing down allowance of 18% can be claimed on the final balance of main pool costs.
          |If the final balance before claiming WDA is £1,000 or less, a small pool allowance can be claimed for the full amount instead of the WDA""".stripMargin, optional = true),
      PositiveMonetaryFieldDescription("self-employments", "capitalAllowanceSpecialRatePool",
        """Writing down allowance of 8% can be claimed on the final balance of the special rate pool costs.
          |If the final balance before claiming WDA is £1,000 or less, a small pool allowance can be claimed for the full amount instead of the WDA""".stripMargin, optional = true),
      PositiveMonetaryFieldDescription("self-employments", "businessPremisesRenovationAllowance", "When eligible, BPRA can be claimed for the cost of renovating or repairing unused business premises", optional = true),
      PositiveMonetaryFieldDescription("self-employments", "enhancedCapitalAllowance", "100% capital allowance can be claimed for eligible capital purchases", optional = true),
      PositiveMonetaryFieldDescription("self-employments", "allowancesOnSales",
        """If the business ceases, any balance left in the relevant pool can be claimed after
          |either the selling price or market value has been deducted from the pool balance, as a balancing allowance instead of claiming a WDA""".stripMargin, optional = true)
    )
  }
}
