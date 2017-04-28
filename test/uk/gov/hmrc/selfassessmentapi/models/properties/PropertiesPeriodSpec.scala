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
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class PropertiesPeriodSpec extends JsonSpec with GeneratorDrivenPropertyChecks {

  "PropertiesPeriod" should {
    "round trip FHL properties" in forAll(FHLGen.genPropertiesPeriod(valid = true)) { fhlProps =>
      roundTripJson(fhlProps)
    }

    "round trip Other properties" in forAll(OtherGen.genPropertiesPeriod(valid = true)) { otherProps =>
      roundTripJson(otherProps)
    }
  }

  "validate" should {
    "reject a FHL properties where the `to` date comes before the `from` date" in forAll(
      FHLGen.genPropertiesPeriod(valid = false)) { fhlProps =>
      assertValidationErrorWithCode(fhlProps, "", ErrorCode.INVALID_PERIOD)
    }

    "reject a Other properties where the `to` date comes before the `from` date" in forAll(
      OtherGen.genPropertiesPeriod(valid = false)) { otherProps =>
      assertValidationErrorWithCode(otherProps, "", ErrorCode.INVALID_PERIOD)
    }

  }

  def amountGen(lower: BigDecimal, upper: BigDecimal): Gen[BigDecimal] =
    for {
      value <- Gen.chooseNum(lower.intValue(), upper.intValue())
    } yield BigDecimal(value)

  val amount: Gen[BigDecimal] = amountGen(1000, 5000)

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

    def genFinancials: Gen[FHL.Financials] =
      (for {
        incomes <- Gen.option(genIncomes)
        expenses <- Gen.option(genExpenses)
      } yield FHL.Financials(incomes, expenses)) suchThat (f => f.expenses.isDefined && f.incomes.isDefined)

    def genPropertiesPeriod(valid: Boolean): Gen[FHL.Properties] =
      for {
        from <- Gen.const(LocalDate.now())
        to <- Gen.oneOf(from, from.plusDays(1))
        financials <- Gen.option(genFinancials)
      } yield
        if (valid)
          FHL.Properties(None, from, to, financials)
        else
          FHL.Properties(None, from, from.minusDays(1), financials)

  }

  object OtherGen {
    val genIncome: Gen[Income] =
      for {
        amount <- amount
        taxDeducted <- Gen.option(amount)
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

    def genFinancials: Gen[Other.Financials] =
      (for {
        incomes <- Gen.option(genIncomes)
        expenses <- Gen.option(genExpenses)
      } yield Other.Financials(incomes, expenses)) suchThat (f => f.expenses.isDefined && f.incomes.isDefined)

    def genPropertiesPeriod(valid: Boolean): Gen[Other.Properties] =
      for {
        from <- Gen.const(LocalDate.now())
        to <- Gen.oneOf(from, from.plusDays(1))
        financials <- Gen.option(genFinancials)
      } yield
        if (valid)
          Other.Properties(None, from, to, financials)
        else
          Other.Properties(None, from, from.minusDays(1), financials)

  }

}
