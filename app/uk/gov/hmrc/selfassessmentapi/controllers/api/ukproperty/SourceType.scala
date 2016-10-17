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

package uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty

import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.SummaryTypes._

object SourceType {

  case object UKProperties extends SourceType {
    override val name = "uk-properties"
    override val documentationName = "UK Properties"

    override def example(sourceId: Option[SourceId] = None): JsValue = toJson(UKProperty.example(sourceId))

    override val summaryTypes: Set[SummaryType] = Set(Incomes, Expenses, TaxesPaid, BalancingCharges, PrivateUseAdjustments)
    override val propertyTypes: Set[YearPropertyType] = Set.empty
    override val title = "Sample UK property"

    override def description(action: String) = s"$action a UK property"

    override val fieldDescriptions = Seq(
      FullFieldDescription(name, "allowances", "Object", "Allowances claimed for this property", optional = true),
      PositiveMonetaryFieldDescription(name, "annualInvestmentAllowance",
        """Annual Investment Allowance can be claimed if equipment was purchased (but not cars) during the year.
          |AIA can be claimed up to a maximum annual amount.
          |AIA can’t be claimed for expenditure on equipment and other items for use in a dwelling house""".stripMargin, optional = true),
      PositiveMonetaryFieldDescription(name, "businessPremisesRenovationAllowance",
        "When eligible, BPRA can be claimed for the cost of renovating or repairing business premises", optional = true),
      PositiveMonetaryFieldDescription(name, "otherCapitalAllowance",
        "The type of capital allowance and amount that can be claimed will depend on the cost, type of asset and other circumstances", optional = true),
      PositiveMonetaryFieldDescription(name, "wearAndTearAllowance",
        """For fully furnished accommodation, wear & tear allowance can be claimed up to 10%
          | of the net rents (including chargeable premiums & reverse premiums) after deducting charges or services that a
          | tenant would usually pay for but which are paid by you (such as Council Tax)""".stripMargin, optional = true),
      FullFieldDescription(name, "adjustments", "Object", "Adjustments for this property", optional = true),
      PositiveMonetaryFieldDescription(name, "lossBroughtForward",
        """If a loss was made in the previous or earlier tax years,
          |this can be used against the profits from this tax year""".stripMargin),
      PositiveMonetaryFieldDescription(name, "rentARoomRelief",
        """For claiming the exempt amount (either £4,250 or £2,125, if let jointly)
          | if any Rent a Room income has been included within Rental Income""".stripMargin, optional = true)
    )
  }

}
