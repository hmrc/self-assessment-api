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

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.Validation._

case class SelfEmploymentPeriod(id: Option[String],
                                from: LocalDate,
                                to: LocalDate,
                                incomes: Option[Incomes],
                                expenses: Option[Expenses])
    extends Period {
  def asSummary: PeriodSummary = PeriodSummary(id.getOrElse(""), from, to)
}

object SelfEmploymentPeriod extends PeriodValidator[SelfEmploymentPeriod] {

  def from(desPeriod: des.SelfEmploymentPeriod): SelfEmploymentPeriod =
    SelfEmploymentPeriod(
      id = desPeriod.id,
      from = LocalDate.parse(desPeriod.from),
      to = LocalDate.parse(desPeriod.to),
      incomes = fromDESIncomes(desPeriod),
      expenses = fromDESExpenses(desPeriod)
    )

  private def fromDESIncomes(desPeriod: des.SelfEmploymentPeriod): Option[Incomes] = {
    desPeriod.financials.flatMap(_.incomes.map { incomes =>
      Incomes(turnover = incomes.turnover.map(SimpleIncome(_)), other = incomes.other.map(SimpleIncome(_)))
    })
  }

  private def fromDESExpenses(desPeriod: des.SelfEmploymentPeriod): Option[Expenses] = {
    desPeriod.financials.flatMap(_.deductions.map { deductions =>
      Expenses(
        cisPaymentsToSubcontractors = deductions.constructionIndustryScheme.map(deduction =>
          Expense(deduction.amount, deduction.disallowableAmount)),
        depreciation =
          deductions.depreciation.map(deduction => Expense(deduction.amount, deduction.disallowableAmount)),
        costOfGoodsBought =
          deductions.costOfGoods.map(deduction => Expense(deduction.amount, deduction.disallowableAmount)),
        professionalFees =
          deductions.professionalFees.map(deduction => Expense(deduction.amount, deduction.disallowableAmount)),
        badDebt = deductions.badDebt.map(deduction => Expense(deduction.amount, deduction.disallowableAmount)),
        adminCosts = deductions.adminCosts.map(deduction => Expense(deduction.amount, deduction.disallowableAmount)),
        advertisingCosts =
          deductions.advertisingCosts.map(deduction => Expense(deduction.amount, deduction.disallowableAmount)),
        financialCharges =
          deductions.financialCharges.map(deduction => Expense(deduction.amount, deduction.disallowableAmount)),
        interest = deductions.interest.map(deduction => Expense(deduction.amount, deduction.disallowableAmount)),
        maintenanceCosts =
          deductions.maintenanceCosts.map(deduction => Expense(deduction.amount, deduction.disallowableAmount)),
        premisesRunningCosts =
          deductions.premisesRunningCosts.map(deduction => Expense(deduction.amount, deduction.disallowableAmount)),
        staffCosts = deductions.staffCosts.map(deduction => Expense(deduction.amount, deduction.disallowableAmount)),
        travelCosts = deductions.travelCosts.map(deduction => Expense(deduction.amount, deduction.disallowableAmount)),
        other = deductions.other.map(deduction => Expense(deduction.amount, deduction.disallowableAmount))
      )
    })
  }

  implicit val writes: Writes[SelfEmploymentPeriod] = Json.writes[SelfEmploymentPeriod]

  private def financialsValidator(period: SelfEmploymentPeriod): Boolean =
    period.incomes.exists(_.hasIncomes)  || period.expenses.exists(_.hasExpenses)

  implicit val reads: Reads[SelfEmploymentPeriod] = (
    Reads.pure(None) and
      (__ \ "from").read[LocalDate] and
      (__ \ "to").read[LocalDate] and
      (__ \ "incomes").readNullable[Incomes] and
      (__ \ "expenses").readNullable[Expenses]
  )(SelfEmploymentPeriod.apply _)
    .validate(
      Seq(Validation(Seq("from", "to"),
                     periodDateValidator,
                     ValidationError("the period 'from' date should come before the 'to' date",
                                     ErrorCode.INVALID_PERIOD)),
          Validation(Seq("incomes", "expenses"),
                     financialsValidator,
                     ValidationError("No incomes and expenses are supplied", ErrorCode.NO_INCOMES_AND_EXPENSES))))

}
