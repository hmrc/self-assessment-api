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
import play.api.libs.json.{Json, Reads, Writes, _}
import uk.gov.hmrc.selfassessmentapi.models.Validation._
import uk.gov.hmrc.selfassessmentapi.models.{ErrorCode, Expense, Validation}

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
                    other: Option[Expense] = None) {

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
  private val validationError =
    ValidationError("disallowableAmount must be equal to or less than amount", ErrorCode.INVALID_DISALLOWABLE_AMOUNT)
  implicit val writes: Writes[Expenses] = Json.writes[Expenses]
  implicit val reads: Reads[Expenses] = Json
    .reads[Expenses]
    .validate(
      Seq(
        Validation[Expenses](
          JsPath \ "depreciation" \ "disallowableAmount",
          _.depreciation.forall(e => e.disallowableAmount.forall(_ == e.amount)),
          ValidationError("the disallowableAmount for depreciation expenses must be the same as the amount",
                          ErrorCode.DEPRECIATION_DISALLOWABLE_AMOUNT)
        ),
        Validation(JsPath \ "costOfGoodsBought" \ "disallowableAmount",
                   _.costOfGoodsBought.forall(validDisallowableAmount),
                   validationError),
        Validation(JsPath \ "cisPaymentsToSubcontractors" \ "disallowableAmount",
                   _.cisPaymentsToSubcontractors.forall(validDisallowableAmount),
                   validationError),
        Validation(JsPath \ "staffCosts" \ "disallowableAmount",
                   _.staffCosts.forall(validDisallowableAmount),
                   validationError),
        Validation(JsPath \ "travelCosts" \ "disallowableAmount",
                   _.travelCosts.forall(validDisallowableAmount),
                   validationError),
        Validation(JsPath \ "premisesRunningCosts" \ "disallowableAmount",
                   _.premisesRunningCosts.forall(validDisallowableAmount),
                   validationError),
        Validation(JsPath \ "maintenanceCosts" \ "disallowableAmount",
                   _.maintenanceCosts.forall(validDisallowableAmount),
                   validationError),
        Validation(JsPath \ "adminCosts" \ "disallowableAmount",
                   _.adminCosts.forall(validDisallowableAmount),
                   validationError),
        Validation(JsPath \ "advertisingCosts" \ "disallowableAmount",
                   _.advertisingCosts.forall(validDisallowableAmount),
                   validationError),
        Validation(JsPath \ "interest" \ "disallowableAmount",
                   _.interest.forall(validDisallowableAmount),
                   validationError),
        Validation(JsPath \ "financialCharges" \ "disallowableAmount",
                   _.financialCharges.forall(validDisallowableAmount),
                   validationError),
        Validation(JsPath \ "badDebt" \ "disallowableAmount",
                   _.badDebt.forall(validDisallowableAmount),
                   validationError),
        Validation(JsPath \ "professionalFees" \ "disallowableAmount",
                   _.professionalFees.forall(validDisallowableAmount),
                   validationError),
        Validation(JsPath \ "other" \ "disallowableAmount", _.other.forall(validDisallowableAmount), validationError)
      ))

  private def validDisallowableAmount(expense: Expense) =
    expense.disallowableAmount.forall(_ <= expense.amount)
}
