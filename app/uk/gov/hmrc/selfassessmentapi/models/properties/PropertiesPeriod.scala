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

package uk.gov.hmrc.selfassessmentapi.models.properties

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode.BOTH_EXPENSES_SUPPLIED
import uk.gov.hmrc.selfassessmentapi.models.Validation._
import uk.gov.hmrc.selfassessmentapi.models.{ExpensesDef, _}

object FHL {

  case class Properties(id: Option[String], from: LocalDate, to: LocalDate, financials: Option[Financials])
    extends Period {
    def asSummary: PeriodSummary = PeriodSummary(id.getOrElse(""), from, to)
  }

  object Properties extends PeriodValidator[Properties] {

    implicit val writes: Writes[Properties] = (
      (__ \ "id").writeNullable[String] and
        (__ \ "from").write[LocalDate] and
        (__ \ "to").write[LocalDate] and
        (__ \ "incomes").writeNullable[Incomes] and
        (__ \ "expenses").writeNullable[Expenses]
      ) (p => (p.id, p.from, p.to, p.financials.flatMap(_.incomes), p.financials.flatMap(_.expenses)))

    private def financialsValidator(period: Properties): Boolean = {
      val incomePassed = period.financials.exists(_.incomes.exists(_.hasIncomes))
      val expensesPassed = period.financials.exists(_.expenses.exists(_.hasExpenses))
      val consolidatedExpensesPassed = period.financials.exists(_.expenses.exists(_.consolidatedExpenses.isDefined))
      incomePassed || expensesPassed || consolidatedExpensesPassed
    }

    private def bothExpensesValidator(period: Properties): Boolean = {
      val expensesPassed = period.financials.exists(_.expenses.isDefined)
      val consolidatedExpensesPassed = period.financials.exists(_.expenses.exists(_.consolidatedExpenses.isDefined))
      !expensesPassed && !consolidatedExpensesPassed || expensesPassed ^ consolidatedExpensesPassed
    }

    implicit val reads: Reads[Properties] = (
      Reads.pure(None) and
        (__ \ "from").read[LocalDate] and
        (__ \ "to").read[LocalDate] and
        (__ \ "incomes").readNullable[Incomes] and
        (__ \ "expenses").readNullable[Expenses]
      ) ((id, from, to, incomes, expenses) => {
      val financials = (incomes, expenses) match {
        case (None, None) => None
        case (inc, exp) => Some(Financials(inc, exp))
      }
      Properties(id, from, to, financials)
    }).validate(
      Seq(Validation(JsPath(),
        periodDateValidator,
        ValidationError("the period 'from' date should come before the 'to' date",
          ErrorCode.INVALID_PERIOD)),
        Validation(JsPath(),
          bothExpensesValidator,
          ValidationError(s"Both expenses and consolidatedExpenses elements cannot be present at the same time", ErrorCode.BOTH_EXPENSES_SUPPLIED)),
        Validation(JsPath(),
          financialsValidator,
          ValidationError("No incomes and expenses are supplied", ErrorCode.NO_INCOMES_AND_EXPENSES))))

    def from(o: des.properties.FHL.Properties): Properties =
      Properties(id = o.transactionReference,
        from = LocalDate.parse(o.from),
        to = LocalDate.parse(o.to),
        financials = Financials.from(o.financials))
  }

  case class Incomes(rentIncome: Option[Income] = None) {
    def hasIncomes: Boolean = rentIncome.isDefined
  }

  object Incomes {
    implicit val format: Format[Incomes] = Json.format[Incomes]

    def from(o: des.properties.FHL.Incomes): Incomes =
      Incomes(rentIncome = o.rentIncome.map(income => Income(amount = income.amount, taxDeducted = income.taxDeducted)))
  }

  case class Expense(amount: Amount)

  object Expense {
    implicit val reads: Reads[Expense] = (__ \ "amount").read[Amount](nonNegativeAmountValidator).map(Expense(_))

    implicit val writes: Writes[Expense] = Json.writes[Expense]
  }

  case class Expenses(premisesRunningCosts: Option[Expense] = None,
                      repairsAndMaintenance: Option[Expense] = None,
                      financialCosts: Option[Expense] = None,
                      professionalFees: Option[Expense] = None,
                      consolidatedExpenses: Option[Expense] = None,
                      other: Option[Expense] = None) {
    def hasExpenses: Boolean =
      premisesRunningCosts.isDefined ||
        repairsAndMaintenance.isDefined ||
        financialCosts.isDefined ||
        professionalFees.isDefined ||
        other.isDefined
  }

  object Expenses {
    implicit val format: Format[Expenses] = Json.format[Expenses]

    def from(o: des.properties.FHL.Deductions): Expenses =
      Expenses(premisesRunningCosts = o.premisesRunningCosts.map(Expense(_)),
        repairsAndMaintenance = o.repairsAndMaintenance.map(Expense(_)),
        financialCosts = o.financialCosts.map(Expense(_)),
        professionalFees = o.professionalFees.map(Expense(_)),
        consolidatedExpenses = o.simplifiedExpenses.map(Expense(_)),
        other = o.other.map(Expense(_)))
  }

  case class Financials(incomes: Option[Incomes] = None, expenses: Option[Expenses] = None)
    extends models.Financials with ExpensesDef[Expenses]

  object Financials {
    implicit val writes: Writes[Financials] = Json.writes[Financials]

    private def financialsValidator(financials: Financials): Boolean =
      financials.incomes.exists(_.hasIncomes) || financials.expenses.exists(_.hasExpenses) || financials.expenses.exists(_.consolidatedExpenses.isDefined)

    private def bothExpensesValidator(financials: Financials): Boolean = {
      val expensesPassed = financials.expenses.isDefined
      val consolidatedExpensesPassed = financials.expenses.exists(_.consolidatedExpenses.isDefined)
      !expensesPassed && !consolidatedExpensesPassed || expensesPassed ^ consolidatedExpensesPassed
    }

    implicit val reads: Reads[Financials] = (
      (__ \ "incomes").readNullable[Incomes] and
        (__ \ "expenses").readNullable[Expenses]
      ) (Financials.apply _)
      .validate(
        Seq(
          Validation(JsPath(),
            financialsValidator,
            ValidationError("No incomes and expenses are supplied", ErrorCode.NO_INCOMES_AND_EXPENSES)),
          Validation(JsPath(),
            bothExpensesValidator,
            ValidationError(s"Both expenses and consolidatedExpenses elements cannot be present at the same time", ErrorCode.BOTH_EXPENSES_SUPPLIED))))

    def from(o: Option[des.properties.FHL.Financials]): Option[Financials] =
      o.flatMap { f =>
        (f.incomes, f.deductions) match {
          case (None, None) => None
          case (incomes, deductions) =>
            Some(Financials(incomes = incomes.map(Incomes.from),
              expenses = deductions.map(Expenses.from).fold[Option[Expenses]](None)(ex => if (ex.hasExpenses) Some(ex) else None)))
        }
      }
  }

}

object Other {

  case class Properties(id: Option[String], from: LocalDate, to: LocalDate, financials: Option[Financials])
    extends Period {
    def asSummary: PeriodSummary = PeriodSummary(id.getOrElse(""), from, to)
  }

  object Properties extends PeriodValidator[Properties] {

    implicit val writes: Writes[Properties] = (
      (__ \ "id").writeNullable[String] and
        (__ \ "from").write[LocalDate] and
        (__ \ "to").write[LocalDate] and
        (__ \ "incomes").writeNullable[Incomes] and
        (__ \ "expenses").writeNullable[Expenses]
      ) (p => (p.id, p.from, p.to, p.financials.flatMap(_.incomes), p.financials.flatMap(_.expenses)))

    private def financialsValidator(period: Properties): Boolean = {
      val incomePassed = period.financials.exists(_.incomes.exists(_.hasIncomes))
      val expensesPassed = period.financials.exists(_.expenses.exists(_.hasExpenses))
      val consolidatedExpensesPassed = period.financials.exists(_.expenses.exists(_.consolidatedExpenses.isDefined))
      incomePassed || expensesPassed || consolidatedExpensesPassed
    }

    private def bothExpensesValidator(period: Properties): Boolean = {
      val expensesPassed = period.financials.exists(_.expenses.exists(_.hasExpenses))
      val consolidatedExpensesPassed = period.financials.exists(_.expenses.exists(_.consolidatedExpenses.isDefined))
      val result = !expensesPassed && !consolidatedExpensesPassed || expensesPassed ^ consolidatedExpensesPassed
      result
    }

    implicit val reads: Reads[Properties] = (
      Reads.pure(None) and
        (__ \ "from").read[LocalDate] and
        (__ \ "to").read[LocalDate] and
        (__ \ "incomes").readNullable[Incomes] and
        (__ \ "expenses").readNullable[Expenses]
      ) ((id, from, to, incomes, expenses) => {
      val financials = (incomes, expenses) match {
        case (None, None) => None
        case (inc, exp) => Some(Financials(inc, exp))
      }
      Properties(id, from, to, financials)
    }).validate(
      Seq(Validation(JsPath(),
        periodDateValidator,
        ValidationError("the period 'from' date should come before the 'to' date",
          ErrorCode.INVALID_PERIOD)),
        Validation(JsPath(),
          bothExpensesValidator,
          ValidationError(s"Both expenses and consolidatedExpenses elements cannot be present at the same time", ErrorCode.BOTH_EXPENSES_SUPPLIED)),
        Validation(JsPath(),
          financialsValidator,
          ValidationError("No incomes and expenses are supplied", ErrorCode.NO_INCOMES_AND_EXPENSES))))

    def from(o: des.properties.Other.Properties): Properties =
      Properties(id = o.transactionReference,
        from = LocalDate.parse(o.from),
        to = LocalDate.parse(o.to),
        financials = Financials.from(o.financials))
  }

  case class Incomes(rentIncome: Option[Income] = None,
                     premiumsOfLeaseGrant: Option[Income] = None,
                     reversePremiums: Option[Income] = None) {
    def hasIncomes: Boolean =
      rentIncome.isDefined ||
        premiumsOfLeaseGrant.isDefined ||
        reversePremiums.isDefined
  }

  object Incomes {

    implicit val format: Format[Incomes] =
      Json.format[Incomes]

    def from(o: des.properties.Other.Incomes): Incomes =
      Incomes(
        rentIncome = o.rentIncome.map(income => Income(amount = income.amount, taxDeducted = income.taxDeducted)),
        premiumsOfLeaseGrant = o.premiumsOfLeaseGrant.map(Income(_, None)),
        reversePremiums = o.reversePremiums.map(Income(_, None)))
  }

  case class Expense(amount: Amount)

  object Expense {
    implicit val reads: Reads[Expense] = (__ \ "amount").read[Amount](nonNegativeAmountValidator).map(Expense(_))

    implicit val writes: Writes[Expense] = Json.writes[Expense]
  }

  case class Expenses(premisesRunningCosts: Option[Expense] = None,
                      repairsAndMaintenance: Option[Expense] = None,
                      financialCosts: Option[Expense] = None,
                      professionalFees: Option[Expense] = None,
                      costOfServices: Option[Expense] = None,
                      consolidatedExpenses: Option[Expense] = None,
                      other: Option[Expense] = None) {
    def hasExpenses: Boolean =
      premisesRunningCosts.isDefined ||
        repairsAndMaintenance.isDefined ||
        financialCosts.isDefined ||
        professionalFees.isDefined ||
        costOfServices.isDefined ||
        other.isDefined
  }

  object Expenses {
    implicit val format: Format[Expenses] = Json.format[Expenses]

    def from(o: des.properties.Other.Deductions): Expenses =
      Expenses(premisesRunningCosts = o.premisesRunningCosts.map(Expense(_)),
        repairsAndMaintenance = o.repairsAndMaintenance.map(Expense(_)),
        financialCosts = o.financialCosts.map(Expense(_)),
        professionalFees = o.professionalFees.map(Expense(_)),
        costOfServices = o.costOfServices.map(Expense(_)),
        consolidatedExpenses = o.simplifiedExpenses.map(Expense(_)),
        other = o.other.map(Expense(_)))
  }

  case class Financials(incomes: Option[Incomes] = None, expenses: Option[Expenses] = None)
    extends models.Financials with ExpensesDef[Expenses]

  object Financials {
    implicit val writes: Writes[Financials] = Json.writes[Financials]

    private def financialsValidator(financials: Financials): Boolean =
      financials.incomes.exists(_.hasIncomes) || financials.expenses.exists(_.hasExpenses)

    private def bothExpensesValidator(financials: Financials): Boolean = {
      val expensesPassed = financials.expenses.exists(_.hasExpenses)
      val consolidatedExpensesPassed = financials.expenses.exists(_.consolidatedExpenses.isDefined)
      !expensesPassed && !consolidatedExpensesPassed || expensesPassed ^ consolidatedExpensesPassed
    }

    implicit val reads: Reads[Financials] = (
      (__ \ "incomes").readNullable[Incomes] and
        (__ \ "expenses").readNullable[Expenses]
      ) (Financials.apply _)
      .filter(ValidationError(s"Both expenses and consolidatedExpenses elements cannot be present at the same time",
        BOTH_EXPENSES_SUPPLIED))(_.singleExpensesTypeSpecified)
      .validate(
        Seq(
          Validation(JsPath(),
            financialsValidator,
            ValidationError("No incomes and expenses are supplied", ErrorCode.NO_INCOMES_AND_EXPENSES)),
          Validation(JsPath(),
            bothExpensesValidator,
            ValidationError(s"Both expenses and consolidatedExpenses elements cannot be present at the same time", ErrorCode.BOTH_EXPENSES_SUPPLIED))))


    def from(o: Option[des.properties.Other.Financials]): Option[Financials] =
      o.flatMap { f =>
        (f.incomes, f.deductions) match {
          case (None, None) => None
          case (incomes, deductions) =>
            Some(Financials(incomes = incomes.map(Incomes.from),
              expenses = deductions.map(Expenses.from).fold[Option[Expenses]](None)(ex => if (ex.hasExpenses) Some(ex) else None)))
        }
      }
  }

}
