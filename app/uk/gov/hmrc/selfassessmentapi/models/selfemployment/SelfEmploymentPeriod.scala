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
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.ExpenseType.ExpenseType
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.IncomeType.IncomeType
import uk.gov.hmrc.selfassessmentapi.models._

case class SelfEmploymentPeriod(id: Option[String], from: LocalDate, to: LocalDate, data: SelfEmploymentPeriodicData) extends Period {
  def asSummary: PeriodSummary = PeriodSummary(id.getOrElse(""), from, to)
}

object SelfEmploymentPeriod extends PeriodValidator[SelfEmploymentPeriod] {

  import uk.gov.hmrc.selfassessmentapi.domain.JsonFormatters.SelfEmploymentFormatters.{expenseTypeFormat, incomeTypeFormat}

  def from(desPeriod: des.SelfEmploymentPeriod): SelfEmploymentPeriod = {
    SelfEmploymentPeriod(
      id = desPeriod.id,
      from = LocalDate.parse(desPeriod.from),
      to = LocalDate.parse(desPeriod.to),
      data = SelfEmploymentPeriodicData(
        incomes = incomes2Map(desPeriod),
        expenses = expenses2Map(desPeriod)
      )
    )
  }

  private def incomes2Map(desPeriod: des.SelfEmploymentPeriod): Map[IncomeType, SimpleIncome] = {
    val incomes = Seq(
      IncomeType.Turnover -> desPeriod.financials.flatMap(_.incomes.flatMap(_.turnover.map(x => SimpleIncome(x)))),
      IncomeType.Other -> desPeriod.financials.flatMap(_.incomes.flatMap(_.other.map(x => SimpleIncome(x))))
    )

    incomes.foldLeft(Map.empty[IncomeType, SimpleIncome]) {
      case (acc, (typ, amt)) => if (amt.isDefined) acc.updated(typ, amt.get) else acc
    }
  }

  private def expenses2Map(desPeriod: des.SelfEmploymentPeriod): Map[ExpenseType, Expense] = {
    val expenses: Seq[(ExpenseType, Option[Expense])] = Seq(
      ExpenseType.CISPaymentsToSubcontractors -> desPeriod.financials.flatMap(_.deductions.flatMap(_.constructionIndustryScheme.map(x => Expense(x.amount, x.disallowableAmount)))),
      ExpenseType.Depreciation -> desPeriod.financials.flatMap(_.deductions.flatMap(_.depreciation.map(x => Expense(x.amount, x.disallowableAmount)))),
      ExpenseType.CostOfGoodsBought -> desPeriod.financials.flatMap(_.deductions.flatMap(_.costOfGoods.map(x => Expense(x.amount, x.disallowableAmount)))),
      ExpenseType.ProfessionalFees -> desPeriod.financials.flatMap(_.deductions.flatMap(_.professionalFees.map(x => Expense(x.amount, x.disallowableAmount)))),
      ExpenseType.BadDebt -> desPeriod.financials.flatMap(_.deductions.flatMap(_.badDebt.map(x => Expense(x.amount, x.disallowableAmount)))),
      ExpenseType.AdminCosts -> desPeriod.financials.flatMap(_.deductions.flatMap(_.adminCosts.map(x => Expense(x.amount, x.disallowableAmount)))),
      ExpenseType.AdvertisingCosts -> desPeriod.financials.flatMap(_.deductions.flatMap(_.advertisingCosts.map(x => Expense(x.amount, x.disallowableAmount)))),
      ExpenseType.FinancialCharges -> desPeriod.financials.flatMap(_.deductions.flatMap(_.financialCharges.map(x => Expense(x.amount, x.disallowableAmount)))),
      ExpenseType.Interest -> desPeriod.financials.flatMap(_.deductions.flatMap(_.interest.map(x => Expense(x.amount, x.disallowableAmount)))),
      ExpenseType.MaintenanceCosts -> desPeriod.financials.flatMap(_.deductions.flatMap(_.maintenanceCosts.map(x => Expense(x.amount, x.disallowableAmount)))),
      ExpenseType.PremisesRunningCosts -> desPeriod.financials.flatMap(_.deductions.flatMap(_.premisesRunningCosts.map(x => Expense(x.amount, x.disallowableAmount)))),
      ExpenseType.StaffCosts -> desPeriod.financials.flatMap(_.deductions.flatMap(_.staffCosts.map(x => Expense(x.amount, x.disallowableAmount)))),
      ExpenseType.TravelCosts -> desPeriod.financials.flatMap(_.deductions.flatMap(_.travelCosts.map(x => Expense(x.amount, x.disallowableAmount)))),
      ExpenseType.Other -> desPeriod.financials.flatMap(_.deductions.flatMap(_.other.map(x => Expense(x.amount, x.disallowableAmount))))
    )

    expenses.foldLeft(Map.empty[ExpenseType, Expense]) {
      case (acc, (typ, amt)) => if (amt.isDefined) acc.updated(typ, amt.get) else acc
    }
  }

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
      (__ \ "incomes").readNullable[Map[IncomeType, SimpleIncome]] and
      (__ \ "expenses").readNullable[Map[ExpenseType, Expense]](depreciationValidator)
    ) (
    (from, to, income, expense) => {
      SelfEmploymentPeriod(None, from, to, SelfEmploymentPeriodicData(income.getOrElse(Map.empty), expense.getOrElse(Map.empty)))
    })
    .filter(ValidationError("the period 'from' date should come before the 'to' date", ErrorCode.INVALID_PERIOD))(periodDateValidator)

  private def depreciationValidator = Reads.of[Map[ExpenseType, Expense]].filter(
    ValidationError("the disallowableAmount for depreciation expenses must be the same as the amount", ErrorCode.DEPRECIATION_DISALLOWABLE_AMOUNT)
  )(_.get(ExpenseType.Depreciation).forall(e => e.amount == e.disallowableAmount.getOrElse(false)))
}
