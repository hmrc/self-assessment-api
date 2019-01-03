/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.r2.selfassessmentapi.models

import play.api.libs.functional.syntax._
import play.api.libs.json._

trait BaseExpense{
  val amount: BigDecimal
  val disallowableAmount: Option[BigDecimal]
}

case class Expense(amount: BigDecimal, disallowableAmount: Option[BigDecimal]) extends BaseExpense
case class ExpenseNegativeOrPositive(amount: BigDecimal, disallowableAmount: Option[BigDecimal]) extends BaseExpense
case class ExpenseProfessionalFees(amount: BigDecimal, disallowableAmount: Option[BigDecimal]) extends BaseExpense

object Expense {
  implicit val writes = Json.writes[Expense]
  implicit val reads: Reads[Expense] = (
    (__ \ "amount").read[BigDecimal](nonNegativeAmountValidator) and
    (__ \ "disallowableAmount").readNullable[BigDecimal](nonNegativeAmountValidator)
    ) (Expense.apply _)
}

object ExpenseNegativeOrPositive {
  implicit val writes = Json.writes[ExpenseNegativeOrPositive]
  implicit val reads: Reads[ExpenseNegativeOrPositive] = (
    (__ \ "amount").read[BigDecimal](positiveOrNegativeAmountValidator) and
      (__ \ "disallowableAmount").readNullable[BigDecimal](positiveOrNegativeAmountValidator)
    ) (ExpenseNegativeOrPositive.apply _)
}

object ExpenseProfessionalFees {
  implicit val writes = Json.writes[ExpenseProfessionalFees]
  implicit val reads: Reads[ExpenseProfessionalFees] = (
    (__ \ "amount").read[BigDecimal](positiveOrNegativeAmountValidator) and
      (__ \ "disallowableAmount").readNullable[BigDecimal](nonNegativeAmountValidator)
    ) (ExpenseProfessionalFees.apply _)
}