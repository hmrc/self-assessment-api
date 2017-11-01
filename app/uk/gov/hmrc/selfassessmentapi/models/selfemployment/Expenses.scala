/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import play.api.libs.json.{Json, Reads, Writes}
import uk.gov.hmrc.selfassessmentapi.models.Amount

case class Expenses(costOfGoodsBought: Option[Amount] = None,
                    cisPaymentsToSubcontractors: Option[Amount] = None,
                    staffCosts: Option[Amount] = None,
                    travelCosts: Option[Amount] = None,
                    premisesRunningCosts: Option[Amount] = None,
                    maintenanceCosts: Option[Amount] = None,
                    adminCosts: Option[Amount] = None,
                    advertisingCosts: Option[Amount] = None,
                    interest: Option[Amount] = None,
                    financialCharges: Option[Amount] = None,
                    badDebt: Option[Amount] = None,
                    professionalFees: Option[Amount] = None,
                    depreciation: Option[Amount] = None,
                    other: Option[Amount] = None) {

  def hasExpenses: Boolean =
    costOfGoodsBought.isDefined ||
      cisPaymentsToSubcontractors.isDefined ||
      staffCosts.isDefined ||
      travelCosts.isDefined ||
      premisesRunningCosts.isDefined ||
      maintenanceCosts.isDefined ||
      adminCosts.isDefined ||
      advertisingCosts.isDefined ||
      interest.isDefined ||
      financialCharges.isDefined ||
      badDebt.isDefined ||
      professionalFees.isDefined ||
      depreciation.isDefined ||
      other.isDefined
}

object Expenses {
  implicit val writes: Writes[Expenses] = Json.writes[Expenses]
  implicit val reads: Reads[Expenses] = Json.reads[Expenses]
}
