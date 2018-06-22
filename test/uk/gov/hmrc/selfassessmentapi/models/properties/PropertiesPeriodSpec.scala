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
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.models.Generators._

class PropertiesPeriodSpec extends JsonSpec with GeneratorDrivenPropertyChecks {

  "PropertiesPeriod" should {
    "round trip FHL properties" in forAll(FHLGen.genPropertiesPeriod())(roundTripJson(_))

    "round trip Other properties" in forAll(OtherGen.genPropertiesPeriod())(roundTripJson(_))
  }

  "validate" should {
    "reject FHL properties where the `to` date comes before the `from` date" in
      forAll(FHLGen.genPropertiesPeriod(invalidPeriod = true)) { fhlProps =>
        assertValidationErrorsWithCode[FHL.Properties](Json.toJson(fhlProps), Map("" -> Seq(ErrorCode.INVALID_PERIOD)))
      }

    "reject Other properties where the `to` date comes before the `from` date" in
      forAll(OtherGen.genPropertiesPeriod(invalidPeriod = true)) { otherProps =>
        assertValidationErrorsWithCode[Other.Properties](Json.toJson(otherProps),
                                                         Map("" -> Seq(ErrorCode.INVALID_PERIOD)))
      }

    "reject FHL properties that has null financials" in
      forAll(FHLGen.genPropertiesPeriod(nullFinancials = true)) { fhlProps =>
        assertValidationErrorsWithCode[FHL.Properties](Json.toJson(fhlProps),
                                                       Map("" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES)))
      }

    "reject Other properties that has null financials" in
      forAll(OtherGen.genPropertiesPeriod(nullFinancials = true)) { otherProps =>
        assertValidationErrorsWithCode[Other.Properties](Json.toJson(otherProps),
                                                         Map("" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES)))
      }

    "reject FHL properties that has both expenses" in
      forAll(FHLGen.genPropertiesPeriod(bothExpenses = true)) { fhlProps =>
        assertValidationErrorsWithCode[FHL.Properties](Json.toJson(fhlProps),
                                                       Map("" -> Seq(ErrorCode.BOTH_EXPENSES_SUPPLIED)))
      }

    "reject Other properties that has both expenses" in
      forAll(OtherGen.genPropertiesPeriod(bothExpenses = true)) { otherProps =>
        assertValidationErrorsWithCode[Other.Properties](Json.toJson(otherProps),
                                                         Map("" -> Seq(ErrorCode.BOTH_EXPENSES_SUPPLIED)))
      }

    "reject FHL properties where the `to` date comes before the `from` date and has null financials" in
      forAll(FHLGen.genPropertiesPeriod(invalidPeriod = true, nullFinancials = true)) { fhlProps =>
        assertValidationErrorsWithCode[FHL.Properties](
          Json.toJson(fhlProps),
          Map("" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES, ErrorCode.INVALID_PERIOD)))

      }

    "reject Other properties where the `to` date comes before the `from` date and has null financials" in
      forAll(OtherGen.genPropertiesPeriod(invalidPeriod = true, nullFinancials = true)) { otherProps =>
        assertValidationErrorsWithCode[Other.Properties](
          Json.toJson(otherProps),
          Map("" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES, ErrorCode.INVALID_PERIOD)))
      }

    "accept Other Properties where only consolidated expenses are passed" in
      forAll(OtherGen.genPropertiesPeriod(consolidatedExpenses = true)) { otherProps =>
        assertJsonValidationPasses[Other.Properties](
          Json.toJson(otherProps))
      }

    "accept Other Properties where only residential expenses are passed" in
      forAll(OtherGen.genPropertiesPeriod(onlyResidentialExpenses = true)) { otherProps =>
        assertJsonValidationPasses[Other.Properties](
          Json.toJson(otherProps))
      }

    "accept Other Properties where only expenses are passed" in
      forAll(OtherGen.genPropertiesPeriod()) { otherProps =>
        assertJsonValidationPasses[Other.Properties](
          Json.toJson(otherProps))
      }
  }

  val amount: Gen[BigDecimal] = amountGen(0, 5000)

  object FHLGen {
    val genIncome: Gen[Income] =
      for {
        amount <- amount
        taxDeducted <- Gen.option(amountGen(0, amount))
      } yield Income(amount, taxDeducted)

    val genIncomes: Gen[FHL.Incomes] =
      for {
        rentIncome <- Gen.option(genIncome)
      } yield FHL.Incomes(rentIncome = rentIncome)

    val genExpense: Gen[FHL.Expense] = for (amount <- amount) yield FHL.Expense(amount)

    val genExpenses: Gen[FHL.Expenses] =
      for {
        premisesRunningCosts <- Gen.option(genExpense)
        repairsAndMaintenance <- Gen.option(genExpense)
        financialCosts <- Gen.option(genExpense)
        professionalFees <- Gen.option(genExpense)
        other <- Gen.option(genExpense)
      } yield
        FHL.Expenses(
          premisesRunningCosts = premisesRunningCosts,
          repairsAndMaintenance = repairsAndMaintenance,
          financialCosts = financialCosts,
          professionalFees = professionalFees,
          other = other
        )

    val genExpensesBoth: Gen[FHL.Expenses] =
      for {
        premisesRunningCosts <- Gen.option(genExpense)
        repairsAndMaintenance <- Gen.option(genExpense)
        financialCosts <- Gen.option(genExpense)
        professionalFees <- Gen.option(genExpense)
        consolidatedExpenses <- Gen.option(genExpense)
        other <- Gen.option(genExpense)
      } yield
        FHL.Expenses(
          premisesRunningCosts = premisesRunningCosts,
          repairsAndMaintenance = repairsAndMaintenance,
          financialCosts = financialCosts,
          professionalFees = professionalFees,
          consolidatedExpenses = consolidatedExpenses,
          other = other
        )

    val genConsolidatedExpenses: Gen[FHL.Expenses] =
      for {
        consolidatedExpenses <- Gen.option(genExpense)
      } yield FHL.Expenses(consolidatedExpenses = consolidatedExpenses)

    val genFinancialsExpenses: Gen[FHL.Financials] =
      (for {
        incomes <- Gen.option(genIncomes)
        expenses <- Gen.option(genExpenses)
      } yield FHL.Financials(incomes, expenses)) retryUntil { f =>
        f.incomes.exists(_.hasIncomes) || f.expenses.exists(_.hasExpenses)
      }

    val genFinancialsConsolidatedExpenses: Gen[FHL.Financials] =
      (for {
        incomes <- Gen.option(genIncomes)
        expenses <- Gen.option(genConsolidatedExpenses)
      } yield FHL.Financials(incomes, expenses)) retryUntil { f =>
        f.incomes.exists(_.hasIncomes) || f.expenses.exists(_.consolidatedExpenses.isDefined)
      }

    val genBothExpensesFinancials: Gen[FHL.Financials] =
      (for {
        incomes <- Gen.option(genIncomes)
        expenses <- Gen.option(genExpensesBoth)
      } yield FHL.Financials(incomes, expenses)) retryUntil { f =>
        f.expenses.exists(_.hasExpenses) && f.expenses.exists(_.consolidatedExpenses.isDefined)
      }

    def genPropertiesPeriod(invalidPeriod: Boolean = false,
                            nullFinancials: Boolean = false,
                            consolidatedExpenses: Boolean = false,
                            bothExpenses: Boolean = false): Gen[FHL.Properties] =
      for {
        emptyFinancials <- Gen.option(FHL.Financials(None, None))
        financials <- genFinancialsExpenses
        consolidatedFinancials <- genFinancialsConsolidatedExpenses
        bothExpensesFinancials <- genBothExpensesFinancials
      } yield {
        val from = LocalDate.now()
        val to = from.plusDays(1)
        FHL.Properties(
          None,
          if (invalidPeriod) to else from,
          if (invalidPeriod) from else to,
          if (nullFinancials) emptyFinancials
          else if (bothExpenses) Some(bothExpensesFinancials)
          else if (consolidatedExpenses) Some(consolidatedFinancials)
          else Some(financials)
        )
      }
  }

  object OtherGen {
    val genIncome: Gen[Income] =
      for {
        amount <- amount
        taxDeducted <- Gen.option(amountGen(0, amount))
      } yield Income(amount, taxDeducted)

    val genIncomes: Gen[Other.Incomes] =
      for {
        rentIncome <- Gen.option(genIncome)
        premiumsOfLeaseGrant <- Gen.option(genIncome)
        reversePremiums <- Gen.option(genIncome)
        otherIncome <- Gen.option(genIncome)
      } yield Other.Incomes(rentIncome, premiumsOfLeaseGrant, reversePremiums, otherIncome)

    val genExpense: Gen[Other.Expense] = for (amount <- amount) yield Other.Expense(amount)

    val genExpenses: Gen[Other.Expenses] =
      for {
        premisesRunningCosts <- Gen.option(genExpense)
        repairsAndMaintenance <- Gen.option(genExpense)
        financialCosts <- Gen.option(genExpense)
        professionalFees <- Gen.option(genExpense)
        costOfServices <- Gen.option(genExpense)
        residentialFinancialCost <- Gen.option(genExpense)
        other <- Gen.option(genExpense)
      } yield
        Other
          .Expenses(premisesRunningCosts = premisesRunningCosts,
            repairsAndMaintenance = repairsAndMaintenance,
            financialCosts = financialCosts,
            professionalFees = professionalFees,
            costOfServices = costOfServices,
            residentialFinancialCost = residentialFinancialCost,
            other = other)

    val genExpensesBoth: Gen[Other.Expenses] =
      for {
        premisesRunningCosts <- Gen.option(genExpense)
        repairsAndMaintenance <- Gen.option(genExpense)
        financialCosts <- Gen.option(genExpense)
        professionalFees <- Gen.option(genExpense)
        costOfServices <- Gen.option(genExpense)
        consolidatedExpenses <- Gen.option(genExpense)
        residentialFinancialCost <- Gen.option(genExpense)
        other <- Gen.option(genExpense)
      } yield
        Other
          .Expenses(premisesRunningCosts,
                    repairsAndMaintenance,
                    financialCosts,
                    professionalFees,
                    costOfServices,
                    consolidatedExpenses,
                    residentialFinancialCost,
                    other)

    val genConsolidatedExpenses: Gen[Other.Expenses] =
      for {
        consolidatedExpenses <- Gen.option(genExpense)
        residentialFinancialCost <- Gen.option(genExpense)
      } yield Other.Expenses(consolidatedExpenses = consolidatedExpenses, residentialFinancialCost = residentialFinancialCost)

    val genResidentialExpenses: Gen[Other.Expenses] =
      for {
        residentialFinancialCost <- Gen.option(genExpense)
      } yield Other.Expenses(residentialFinancialCost = residentialFinancialCost)


    val genFinancialsExpenses: Gen[Other.Financials] =
      (for {
        incomes <- Gen.option(genIncomes)
        expenses <- Gen.option(genExpenses)
      } yield Other.Financials(incomes, expenses)) retryUntil { f =>
        f.incomes.exists(_.hasIncomes) || f.expenses.exists(_.hasExpenses)
      }

    val genFinancialsConsolidatedExpenses: Gen[Other.Financials] =
      (for {
        incomes <- Gen.option(genIncomes)
        expenses <- Gen.option(genConsolidatedExpenses)
      } yield Other.Financials(incomes, expenses)) retryUntil { f =>
        f.incomes.exists(_.hasIncomes) || f.expenses.exists(_.consolidatedExpenses.isDefined) && f.expenses.exists(_.residentialFinancialCost.isDefined)
      }

    val genBothExpensesFinancials: Gen[Other.Financials] =
      (for {
        incomes <- Gen.option(genIncomes)
        expenses <- Gen.option(genExpensesBoth)
      } yield Other.Financials(incomes, expenses)) retryUntil { f =>
        f.expenses.exists(_.hasExpenses) && f.expenses.exists(_.consolidatedExpenses.isDefined)
      }

    val genResidentialFinancials: Gen[Other.Financials] =
      (for {
        incomes <- Gen.option(genIncomes)
        expenses <- Gen.option(genConsolidatedExpenses)
      } yield Other.Financials(incomes, expenses)) retryUntil { f =>
        f.expenses.exists(_.residentialFinancialCost.isDefined)
      }

    def genPropertiesPeriod(invalidPeriod: Boolean = false,
                            nullFinancials: Boolean = false,
                            consolidatedExpenses: Boolean = false,
                            bothExpenses: Boolean = false,
                            onlyResidentialExpenses: Boolean = false): Gen[Other.Properties] =
      for {
        emptyFinancials <- Gen.option(Other.Financials(None, None))
        financials <- genFinancialsExpenses
        consolidatedFinancials <- genFinancialsConsolidatedExpenses
        bothExpensesFinancials <- genBothExpensesFinancials
        residentialFinancials <- genResidentialFinancials
      } yield {
        val from = LocalDate.now()
        val to = from.plusDays(1)
        Other.Properties(
          None,
          if (invalidPeriod) to else from,
          if (invalidPeriod) from else to,
          if (nullFinancials) emptyFinancials
          else if (bothExpenses) Some(bothExpensesFinancials)
          else if (consolidatedExpenses) Some(consolidatedFinancials)
          else if (onlyResidentialExpenses) Some(residentialFinancials)
          else Some(financials)
        )
      }
  }
}
