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

import play.api.data.validation.ValidationError
import play.api.libs.json.{Json, Reads, Writes}
import uk.gov.hmrc.selfassessmentapi.models.{ErrorCode, Expense}

case class Expenses(costOfGoodsBought: Option[Expense] = None,
                    cisPaymentsToSubcontractors: Option[Expense] = None,
                    staffCosts: Option[Expense] = None,
                    travelCosts: Option[Expense] = None,
                    premisesRunningCosts: Option[Expense] = None,
                    maintenanceCosts: Option[Expense] = None,
                    adminCosts: Option[Expense] = None,
                    advertisingCosts: Option[Expense] = None,
                    interest: Option[Expense] = None,
                    financialCharges: Option[Expense] = None,
                    badDebt: Option[Expense] = None,
                    professionalFees: Option[Expense] = None,
                    depreciation: Option[Expense] = None,
                    other: Option[Expense] = None)

object Expenses {
  implicit val writes: Writes[Expenses] = Json.writes[Expenses]
  implicit val reads: Reads[Expenses] = Json.reads[Expenses].filter(
    ValidationError("the disallowableAmount for depreciation expenses must be the same as the amount", ErrorCode.DEPRECIATION_DISALLOWABLE_AMOUNT)
  )(_.depreciation.forall(e => e.amount == e.disallowableAmount.getOrElse(false)))
}
