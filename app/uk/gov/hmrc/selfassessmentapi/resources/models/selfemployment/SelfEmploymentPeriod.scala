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

package uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.resources.models.{ErrorCode, _}
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.IncomeType.IncomeType

case class SelfEmploymentPeriod(from: LocalDate, to: LocalDate, data: SelfEmploymentPeriodicData) extends Period

object SelfEmploymentPeriod extends PeriodValidator[SelfEmploymentPeriod] {
  import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.SelfEmploymentFormatters.{expenseTypeFormat, incomeTypeFormat}

  implicit val writes: Writes[SelfEmploymentPeriod] = new Writes[SelfEmploymentPeriod] {
    override def writes(period: SelfEmploymentPeriod): JsValue = {
      Json.obj(
        "from" -> period.from.toString,
        "to" -> period.to.toString,
        "incomes" -> period.data.incomes,
        "expenses" -> period.data.expenses
      )
    }
  }

  implicit val reads: Reads[SelfEmploymentPeriod] = (
      (__ \ "from").read[LocalDate] and
      (__ \ "to").read[LocalDate] and
      (__ \ "incomes").readNullable[Map[IncomeType, Income]] and
      (__ \ "expenses").readNullable[Map[ExpenseType, Expense]](depreciationValidator)
    ) (
    (from, to, income, expense) => {
      SelfEmploymentPeriod(from, to, SelfEmploymentPeriodicData(income.getOrElse(Map.empty), expense.getOrElse(Map.empty)))})
    .filter(ValidationError("the period 'from' date should come before the 'to' date", ErrorCode.INVALID_PERIOD))(periodDateValidator)

  private def depreciationValidator = Reads.of[Map[ExpenseType, Expense]].filter(
    ValidationError("the disallowableAmount for depreciation expenses must be the same as the amount", ErrorCode.DEPRECIATION_DISALLOWABLE_AMOUNT)
  )(_.get(ExpenseType.Depreciation).forall(e => e.amount == e.disallowableAmount.getOrElse(false)))
}
