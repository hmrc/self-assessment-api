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
        assertValidationErrorsWithCode[FHL.Properties](Json.toJson(fhlProps),
                                                       Map("/from" -> Seq(ErrorCode.INVALID_PERIOD),
                                                           "/to" -> Seq(ErrorCode.INVALID_PERIOD)))
      }

    "reject Other properties where the `to` date comes before the `from` date" in
      forAll(OtherGen.genPropertiesPeriod(invalidPeriod = true)) { otherProps =>
        assertValidationErrorsWithCode[Other.Properties](Json.toJson(otherProps),
                                                         Map("/from" -> Seq(ErrorCode.INVALID_PERIOD),
                                                             "/to" -> Seq(ErrorCode.INVALID_PERIOD)))
      }

    "reject FHL properties that has null financials" in
      forAll(FHLGen.genPropertiesPeriod(nullFinancials = true)) { fhlProps =>
        assertValidationErrorsWithCode[FHL.Properties](Json.toJson(fhlProps),
                                                       Map("/incomes" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES),
                                                           "/expenses" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES)))
      }

    "reject Other properties that has null financials" in
      forAll(OtherGen.genPropertiesPeriod(nullFinancials = true)) { otherProps =>
        assertValidationErrorsWithCode[Other.Properties](Json.toJson(otherProps),
                                                         Map("/incomes" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES),
                                                             "/expenses" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES)))
      }

    "reject FHL properties where the `to` date comes before the `from` date and has null financials" in
      forAll(FHLGen.genPropertiesPeriod(invalidPeriod = true, nullFinancials = true)) { fhlProps =>
        assertValidationErrorsWithCode[FHL.Properties](Json.toJson(fhlProps),
                                                       Map("/incomes" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES),
                                                           "/expenses" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES),
                                                           "/from" -> Seq(ErrorCode.INVALID_PERIOD),
                                                           "/to" -> Seq(ErrorCode.INVALID_PERIOD)))
      }

    "reject Other properties where the `to` date comes before the `from` date and has null financials" in
      forAll(OtherGen.genPropertiesPeriod(invalidPeriod = true, nullFinancials = true)) { otherProps =>
        assertValidationErrorsWithCode[Other.Properties](Json.toJson(otherProps),
                                                         Map("/incomes" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES),
                                                             "/expenses" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES),
                                                             "/from" -> Seq(ErrorCode.INVALID_PERIOD),
                                                             "/to" -> Seq(ErrorCode.INVALID_PERIOD)))
      }
  }

  val amount: Gen[BigDecimal] = amountGen(0, 5000)

  object FHLGen {
    val genSimpleIncome: Gen[SimpleIncome] = for (amount <- amount) yield SimpleIncome(amount)

    val genIncomes: Gen[FHL.Incomes] =
      for {
        rentIncome <- Gen.option(genSimpleIncome)
      } yield FHL.Incomes(rentIncome = rentIncome)

    val genExpense: Gen[FHL.Expense] = for (amount <- amount) yield FHL.Expense(amount)

    val genExpenses: Gen[FHL.Expenses] =
      for {
        premisesRunningCosts <- Gen.option(genExpense)
        repairsAndMaintenance <- Gen.option(genExpense)
        financialCosts <- Gen.option(genExpense)
        professionalFees <- Gen.option(genExpense)
        other <- Gen.option(genExpense)
      } yield FHL.Expenses(premisesRunningCosts, repairsAndMaintenance, financialCosts, professionalFees, other)

    val genFinancials: Gen[FHL.Financials] =
      (for {
        incomes <- Gen.option(genIncomes)
        expenses <- Gen.option(genExpenses)
      } yield FHL.Financials(incomes, expenses)) suchThat { f =>
        (f.incomes.isDefined && f.incomes.get.hasIncomes) ||
        (f.expenses.isDefined && f.expenses.get.hasExpenses)
      }

    def genPropertiesPeriod(invalidPeriod: Boolean = false, nullFinancials: Boolean = false): Gen[FHL.Properties] =
      for {
        emptyFinancials <- Gen.option(FHL.Financials(None, None))
        financials <- genFinancials
      } yield {
        val from = LocalDate.now()
        val to = from.plusDays(1)
        FHL.Properties(None,
                       if (invalidPeriod) to else from,
                       if (invalidPeriod) from else to,
                       if (nullFinancials) emptyFinancials else Some(financials))
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
      } yield Other.Incomes(rentIncome, premiumsOfLeaseGrant, reversePremiums)

    val genExpense: Gen[Other.Expense] = for (amount <- amount) yield Other.Expense(amount)

    val genExpenses: Gen[Other.Expenses] =
      for {
        premisesRunningCosts <- Gen.option(genExpense)
        repairsAndMaintenance <- Gen.option(genExpense)
        financialCosts <- Gen.option(genExpense)
        professionalFees <- Gen.option(genExpense)
        costOfServices <- Gen.option(genExpense)
        other <- Gen.option(genExpense)
      } yield
        Other
          .Expenses(premisesRunningCosts,
                    repairsAndMaintenance,
                    financialCosts,
                    professionalFees,
                    costOfServices,
                    other)

    val genFinancials: Gen[Other.Financials] =
      (for {
        incomes <- Gen.option(genIncomes)
        expenses <- Gen.option(genExpenses)
      } yield Other.Financials(incomes, expenses)) suchThat { f =>
        (f.incomes.isDefined && f.incomes.get.hasIncomes) ||
        (f.expenses.isDefined && f.expenses.get.hasExpenses)
      }

    def genPropertiesPeriod(invalidPeriod: Boolean = false, nullFinancials: Boolean = false): Gen[Other.Properties] =
      for {
        emptyFinancials <- Gen.option(Other.Financials(None, None))
        financials <- genFinancials
      } yield {
        val from = LocalDate.now()
        val to = from.plusDays(1)
        Other.Properties(None,
                         if (invalidPeriod) to else from,
                         if (invalidPeriod) from else to,
                         if (nullFinancials) emptyFinancials else Some(financials))
      }
  }
}
