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

package uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.domain._

object SourceType {

  case object FurnishedHolidayLettings extends SourceType {
    override val name = "furnished-holiday-lettings"
    override lazy val example: JsValue = toJson(furnishedholidaylettings.FurnishedHolidayLettings.example)
    override val summaryTypes = Seq(SummaryTypes.Incomes,
                                    SummaryTypes.Expenses,
                                    SummaryTypes.PrivateUseAdjustments,
                                    SummaryTypes.BalancingCharges)
    override val title = "Sample furnished holiday lettings"

    override def description(action: String) = s"$action a furnished holiday letting"

    override val fieldDescriptions = Seq(
      FullFieldDescription(name, "name", "String", "Holiday Cottage", "Identifier for the property"),
      FullFieldDescription(name, "propertyLocation", "Enum", PropertyLocationType.values.mkString(", "), "The location of the property"),
      FullFieldDescription(name, "allowances", "Object", "", "Allowances claimed for this property", optional = true),
      PositiveMonetaryFieldDescription(name, "capitalAllowance"),
      FullFieldDescription(name, "adjustments", "Object", "", "Adjustments for this property", optional = true),
      PositiveMonetaryFieldDescription(name, "lossBroughtForward")
    )
  }

}
