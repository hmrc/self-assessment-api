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

package uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.SummaryTypes.{BalancingCharges, Expenses, Incomes, PrivateUseAdjustments}

object SourceType {

  case object FurnishedHolidayLettings extends SourceType {
    override val name = "furnished-holiday-lettings"
    override val documentationName = "Furnished Holiday Lettings"
    override def example(sourceId: Option[SourceId] = None): JsValue = toJson(FurnishedHolidayLetting.example(sourceId))
    override val summaryTypes : Set[SummaryType] = Set(Incomes, Expenses, PrivateUseAdjustments, BalancingCharges)
    override val propertyTypes: Set[AnnualSummaryType] = Set.empty
    override val title = "Sample furnished holiday lettings"

    override def description(action: String) = s"$action a furnished holiday letting"

    override val fieldDescriptions = Seq(
      FullFieldDescription(name, "propertyLocation", "Enum", "The location of the property"),
      FullFieldDescription(name, "allowances", "Object", "Allowances claimed for this property", optional = true),
      PositiveMonetaryFieldDescription(name, "capitalAllowance", "Amount claimed for any equipment or vehicles for your FHL (not other furnished residential lettings)"),
      FullFieldDescription(name, "adjustments", "Object", "Adjustments for this property", optional = true),
      PositiveMonetaryFieldDescription(name, "lossBroughtForward",
        """If a loss was made in the previous or earlier tax years, this can be used against the profits from this tax year.
          |The loss claimed cannot be more than the adjusted profit for this tax year""".stripMargin)
    )
  }

}
