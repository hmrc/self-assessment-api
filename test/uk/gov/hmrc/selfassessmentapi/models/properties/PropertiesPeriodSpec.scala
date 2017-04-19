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

  def amountGen(lower: BigDecimal, upper: BigDecimal): Gen[BigDecimal] =
    for {
      value <- Gen.chooseNum(lower.intValue(), upper.intValue())
    } yield BigDecimal(value)

  val amount: Gen[BigDecimal] = amountGen(1000, 5000)

  val genFHLIncome: Gen[FHL.Income] = for (amount <- amount) yield FHL.Income(amount)

  val genFHLIncomes: Gen[FHL.Incomes] =
    for {
      rentIncome <- Gen.option(genFHLIncome)
    } yield FHL.Incomes(rentIncome = rentIncome)

  val genFHLExpense: Gen[FHL.Expense] = for (amount <- amount) yield FHL.Expense(amount)

  val genFHLExpenses: Gen[FHL.Expenses] =
    for {
      premisesRunningCosts <- Gen.option(genFHLExpense)
      repairsAndMaintenance <- Gen.option(genFHLExpense)
      financialCosts <- Gen.option(genFHLExpense)
      professionalFees <- Gen.option(genFHLExpense)
      other <- Gen.option(genFHLExpense)
    } yield FHL.Expenses(premisesRunningCosts, repairsAndMaintenance, financialCosts, professionalFees, other)

  val genOtherIncome: Gen[Other.Income] =
    for {
      amount <- amount
      taxDeducted <- Gen.option(amount)
    } yield Other.Income(amount, taxDeducted)

  val genOtherIncomes: Gen[Other.Incomes] =
    for {
      rentIncome <- Gen.option(genOtherIncome)
      premiumsOfLeaseGrant <- Gen.option(genOtherIncome)
      reversePremiums <- Gen.option(genOtherIncome)
    } yield Other.Incomes(rentIncome, premiumsOfLeaseGrant, reversePremiums)

  val genOtherExpense: Gen[Other.Expense] = for (amount <- amount) yield Other.Expense(amount)

  val genOtherExpenses: Gen[Other.Expenses] =
    for {
      premisesRunningCosts <- Gen.option(genOtherExpense)
      repairsAndMaintenance <- Gen.option(genOtherExpense)
      financialCosts <- Gen.option(genOtherExpense)
      professionalFees <- Gen.option(genOtherExpense)
      costOfServices <- Gen.option(genOtherExpense)
      other <- Gen.option(genOtherExpense)
    } yield
      Other.Expenses(premisesRunningCosts,
                       repairsAndMaintenance,
                       financialCosts,
                       professionalFees,
                       costOfServices,
                       other)

  def genFHLPropertiesPeriodicData(valid: Boolean): Gen[FHL.Properties] =
    for {
      from <- Gen.const(LocalDate.now())
      to <- Gen.oneOf(from, from.plusDays(1))
      incomes <- Gen.option(genFHLIncomes)
      expenses <- Gen.option(genFHLExpenses)
    } yield
      if (valid)
        FHL.Properties(from, to, FHL.Financials(incomes, expenses))
      else
        FHL.Properties(from, from.minusDays(1), FHL.Financials(incomes, expenses))

  def genOtherPropertiesPeriodicData(valid: Boolean): Gen[Other.Properties] =
    for {
      from <- Gen.const(LocalDate.now())
      to <- Gen.oneOf(from, from.plusDays(1))
      incomes <- Gen.option(genOtherIncomes)
      expenses <- Gen.option(genOtherExpenses)
    } yield
      if (valid)
        Other.Properties(from, to, Other.Financials(incomes, expenses))
      else
        Other.Properties(from, from.minusDays(1), Other.Financials(incomes, expenses))

  "PropertiesPeriod" should {
    "round trip FHL properties" in forAll(genFHLPropertiesPeriodicData(valid = true)) { fhlProps =>
      roundTripJson(fhlProps)
    }

    "round trip Other properties" in forAll(genOtherPropertiesPeriodicData(valid = true)) { otherProps =>
      roundTripJson(otherProps)
    }
  }

  "validate" should {
    "reject a FHL properties where the `to` date comes before the `from` date" in forAll(
      genFHLPropertiesPeriodicData(valid = false)) { fhlProps =>
      assertValidationErrorWithCode(fhlProps, "", ErrorCode.INVALID_PERIOD)
    }

    "reject a Other properties where the `to` date comes before the `from` date" in forAll(
      genOtherPropertiesPeriodicData(valid = false)) { otherProps =>
      assertValidationErrorWithCode(otherProps, "", ErrorCode.INVALID_PERIOD)
    }

  }
}
