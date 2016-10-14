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

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonMarshaller
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.ExpenseType.ExpenseType

object ExpenseType extends Enumeration {
  type ExpenseType = Value
  val CoGBought, CISPaymentsToSubcontractors, StaffCosts, TravelCosts, PremisesRunningCosts, MaintenanceCosts,
  AdminCosts, AdvertisingCosts, Interest, FinancialCharges, BadDebt, ProfessionalFees, Depreciation, Other =
    Value
  implicit val seExpenseTypes = EnumJson.enumFormat(ExpenseType, Some("Self Employment Expense type is invalid"))
}

case class Expense(id: Option[SummaryId] = None,
                   `type`: ExpenseType,
                   amount: BigDecimal,
                   disallowableAmount: BigDecimal)

object Expense extends JsonMarshaller[Expense] {

  implicit val writes = Json.writes[Expense]

  implicit val reads: Reads[Expense] = (
    Reads.pure(None) and
      (__ \ "type").read[ExpenseType] and
      (__ \ "amount").read[BigDecimal](positiveAmountValidator("amount")) and
      (__ \ "disallowableAmount").read[BigDecimal](positiveAmountValidator("disallowableAmount"))
  )(Expense.apply _)
    .filter(ValidationError(
      "the disallowableAmount for Depreciation & Loss/Profit on Sale of Assets must be the same as the amount",
      DEPRECIATION_DISALLOWABLE_AMOUNT)) { expense =>
      expense.`type` != ExpenseType.Depreciation || expense.disallowableAmount == expense.amount
    }
    .filter(ValidationError("disallowableAmount must be less than or equal to the amount",
                            INVALID_DISALLOWABLE_AMOUNT)) { expense =>
      expense.`type` == ExpenseType.Depreciation || expense.disallowableAmount <= expense.amount
    }

  override def example(id: Option[SummaryId]) =
    Expense(id, ExpenseType.CISPaymentsToSubcontractors, BigDecimal(1000), BigDecimal(200))
}
