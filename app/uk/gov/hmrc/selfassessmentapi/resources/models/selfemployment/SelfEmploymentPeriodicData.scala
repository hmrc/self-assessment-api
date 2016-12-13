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

import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.IncomeType.IncomeType
import uk.gov.hmrc.selfassessmentapi.resources.models.{ErrorCode, Expense, Income, PeriodicData}

case class SelfEmploymentPeriodicData(incomes: Map[IncomeType, Income], expenses: Map[ExpenseType, Expense]) extends PeriodicData

object SelfEmploymentPeriodicData {
  import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.SelfEmploymentFormatters.{expenseTypeFormat, incomeTypeFormat}

  implicit val writes: Writes[SelfEmploymentPeriodicData] = Json.writes[SelfEmploymentPeriodicData]

  implicit val reads: Reads[SelfEmploymentPeriodicData] = (
      (__ \ "incomes").readNullable[Map[IncomeType, Income]] and
      (__ \ "expenses").readNullable[Map[ExpenseType, Expense]](depreciationValidator)
    ) ((incomes, expenses) => {SelfEmploymentPeriodicData(incomes.getOrElse(Map.empty), expenses.getOrElse(Map.empty))})

  private def depreciationValidator = Reads.of[Map[ExpenseType, Expense]].filter(
    ValidationError("the disallowableAmount for depreciation expenses must be the same as the amount", ErrorCode.DEPRECIATION_DISALLOWABLE_AMOUNT)
  )(_.get(ExpenseType.Depreciation).forall(e => e.amount == e.disallowableAmount.getOrElse(false)))

}
