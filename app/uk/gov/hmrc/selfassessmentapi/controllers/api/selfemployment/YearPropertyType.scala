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

object YearPropertyType {
  case object Adjustments extends YearPropertyType {
    override val name: String = "adjustments"
    override val documentationName: String = "Adjustments"
    override def example(id: Option[SummaryId]): JsValue = toJson(selfemployment.Adjustments.example)
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
}
