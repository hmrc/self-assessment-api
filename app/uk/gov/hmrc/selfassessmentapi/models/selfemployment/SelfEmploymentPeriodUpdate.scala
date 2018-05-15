/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.models.Validation._
import uk.gov.hmrc.selfassessmentapi.models.{ErrorCode, Validation, _}

case class SelfEmploymentPeriodUpdate(incomes: Option[Incomes],
                                      expenses: Option[Expenses],
                                      consolidatedExpenses: Option[BigDecimal]) extends ExpensesDef[Expenses]

object SelfEmploymentPeriodUpdate {
  implicit val writes: Writes[SelfEmploymentPeriodUpdate] = Json.writes[SelfEmploymentPeriodUpdate]

  private def financialsValidator(sePeriodUpdate: SelfEmploymentPeriodUpdate): Boolean =
    sePeriodUpdate.incomes.exists(_.hasIncomes) || sePeriodUpdate.expenses.exists(_.hasExpenses) || sePeriodUpdate.consolidatedExpenses.isDefined

  implicit val reads: Reads[SelfEmploymentPeriodUpdate] = (
    (__ \ "incomes").readNullable[Incomes] and
    (__ \ "expenses").readNullable[Expenses] and
    (__ \ "consolidatedExpenses").readNullable[BigDecimal](nonNegativeAmountValidator)
  )(SelfEmploymentPeriodUpdate.apply _)
    .filter(ValidationError(s"Both expenses and consolidatedExpenses elements cannot be present at the same time",
      BOTH_EXPENSES_SUPPLIED))(_.singleExpensesTypeSpecified)
    .validate(
      Seq(
        Validation(JsPath(),
                   financialsValidator,
                   ValidationError("No incomes and expenses are supplied", ErrorCode.NO_INCOMES_AND_EXPENSES))))
}
