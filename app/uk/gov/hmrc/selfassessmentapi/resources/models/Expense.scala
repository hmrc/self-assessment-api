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

package uk.gov.hmrc.selfassessmentapi.resources.models

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Expense(amount: Amount, disallowableAmount: Option[Amount])

object Expense {
  implicit val reads: Reads[Expense] = (
    (__ \ "amount").read[Amount](positiveAmountValidator) and
    (__ \ "disallowableAmount").readNullable[Amount](positiveAmountValidator)
    ) (Expense.apply _)
    .filter(ValidationError("disallowableAmount must be equal to or less than amount", ErrorCode.INVALID_DISALLOWABLE_AMOUNT)
    )(disallowableAmountValidator)

  implicit val writes = Json.writes[Expense]

  private def disallowableAmountValidator(expense: Expense) =
    expense.disallowableAmount.forall(expense.amount >= _)
}