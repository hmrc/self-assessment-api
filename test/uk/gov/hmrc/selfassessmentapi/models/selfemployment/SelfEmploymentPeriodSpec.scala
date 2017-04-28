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
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.models.Generators._
import uk.gov.hmrc.selfassessmentapi.models.{ErrorCode, Expense, SimpleIncome}
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class SelfEmploymentPeriodSpec extends JsonSpec with GeneratorDrivenPropertyChecks {
  "SelfEmploymentPeriod" should {
    "round trip" in forAll(genSelfEmploymentPeriod())(roundTripJson(_))

    "return a INVALID_PERIOD error when using a period with a 'from' date that becomes before the 'to' date" in
      forAll(genSelfEmploymentPeriod(invalidPeriod = true)) { period =>
        assertValidationErrorsWithCode[SelfEmploymentPeriod](Json.toJson(period),
                                                             Map("/from" -> Seq(ErrorCode.INVALID_PERIOD),
                                                                 "/to" -> Seq(ErrorCode.INVALID_PERIOD)))
      }

    "return a NO_INCOMES_AND_EXPENSES error when incomes and expenses are not supplied" in
      forAll(genSelfEmploymentPeriod(nullFinancials = true)) { period =>
        assertValidationErrorsWithCode[SelfEmploymentPeriod](
          Json.toJson(period),
          Map("/incomes" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES),
              "/expenses" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES)))
      }

    "return a INVALID_PERIOD and NO_INCOMES_AND_EXPENSES errors when the from and to dates are invalid and incomes and expenses are not supplied" in
      forAll(genSelfEmploymentPeriod(invalidPeriod = true, nullFinancials = true)) { period =>
        assertValidationErrorsWithCode[SelfEmploymentPeriod](Json.toJson(period),
                                                             Map("/incomes" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES),
                                                                 "/expenses" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES),
                                                                 "/from" -> Seq(ErrorCode.INVALID_PERIOD),
                                                                 "/to" -> Seq(ErrorCode.INVALID_PERIOD)))
      }

    "return an error when provided with an empty json body" in
      assertValidationErrorsWithMessage[SelfEmploymentPeriod](Json.parse("{}"),
                                                              Map("/from" -> Seq("error.path.missing"),
                                                                  "/to" -> Seq("error.path.missing")))

    "pass if the from date is equal to the end date" in {
      val period = SelfEmploymentPeriod(id = None,
                                        from = LocalDate.parse("2017-04-01"),
                                        to = LocalDate.parse("2017-04-01"),
                                        incomes = Some(Incomes(turnover = Some(SimpleIncome(0)))),
                                        expenses = None)
      assertValidationPasses(period)
    }
  }

  val amount: Gen[BigDecimal] = amountGen(1000, 5000)

  val genSimpleIncome: Gen[SimpleIncome] = for (amount <- amount) yield SimpleIncome(amount)

  val genIncomes: Gen[Incomes] =
    for {
      turnover <- Gen.option(genSimpleIncome)
      other <- Gen.option(genSimpleIncome)
    } yield Incomes(turnover = turnover, other = other)

  def genExpense(depreciation: Boolean = false): Gen[Expense] =
    for {
      amount <- amount
      disallowableAmount <- Gen.option(amountGen(0, amount))
    } yield Expense(amount = amount, disallowableAmount = if (depreciation) Some(amount) else disallowableAmount)

  val genExpenses: Gen[Expenses] =
    for {
      costOfGoodsBought <- Gen.option(genExpense())
      cisPaymentsToSubcontractors <- Gen.option(genExpense())
      staffCosts <- Gen.option(genExpense())
      travelCosts <- Gen.option(genExpense())
      premisesRunningCosts <- Gen.option(genExpense())
      maintenanceCosts <- Gen.option(genExpense())
      adminCosts <- Gen.option(genExpense())
      advertisingCosts <- Gen.option(genExpense())
      interest <- Gen.option(genExpense())
      financialCharges <- Gen.option(genExpense())
      badDebt <- Gen.option(genExpense())
      professionalFees <- Gen.option(genExpense())
      depreciation <- Gen.option(genExpense(depreciation = true))
      other <- Gen.option(genExpense())
    } yield
      Expenses(costOfGoodsBought = costOfGoodsBought,
               cisPaymentsToSubcontractors = cisPaymentsToSubcontractors,
               staffCosts = staffCosts,
               travelCosts = travelCosts,
               premisesRunningCosts = premisesRunningCosts,
               maintenanceCosts = maintenanceCosts,
               adminCosts = adminCosts,
               advertisingCosts = advertisingCosts,
               interest = interest,
               financialCharges = financialCharges,
               badDebt = badDebt,
               professionalFees = professionalFees,
               depreciation = depreciation,
               other = other)

  def genSelfEmploymentPeriod(invalidPeriod: Boolean = false,
                              nullFinancials: Boolean = false): Gen[SelfEmploymentPeriod] =
    (for {
      incomes <- Gen.option(genIncomes)
      expenses <- Gen.option(genExpenses)
    } yield {
      val from = LocalDate.now()
      val to = from.plusDays(1)
      SelfEmploymentPeriod(None, if (invalidPeriod) to else from, if (invalidPeriod) from else to, incomes, expenses)
    }) suchThat { period =>
      if (nullFinancials)
        period.incomes.isEmpty && period.expenses.isEmpty
      else
        (period.incomes.isDefined && period.incomes.get.hasIncomes) ||
        (period.expenses.isDefined && period.expenses.get.hasExpenses)
    }

}
