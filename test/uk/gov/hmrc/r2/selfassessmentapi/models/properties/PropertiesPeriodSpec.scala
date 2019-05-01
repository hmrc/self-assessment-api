/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.r2.selfassessmentapi.models.properties



import org.joda.time.LocalDate
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.r2.selfassessmentapi.models._
import uk.gov.hmrc.r2.selfassessmentapi.resources.JsonSpec

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

    "accept max boundary R2 amounts for FHL" in
    forAll(fhlR2TestData()) { fhlProps =>
      assertJsonValidationPasses[FHL.Properties](
        Json.toJson(fhlProps)
      )
    }

    "reject over max boundary for rarRentReceived for FHL" in
      forAll(fhlR2TestData(rarRentReceived = 100000000000.00)) { fhlProps =>
        assertValidationErrorsWithCode[FHL.Properties](
          Json.toJson(fhlProps),
          Map("/incomes/rarRentReceived" ->  Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
        )
      }

    "reject over max boundary for travelCosts for FHL" in
      forAll(fhlR2TestData(travelCosts = 100000000000.00)) { fhlProps =>
      assertValidationErrorsWithCode[FHL.Properties](
        Json.toJson(fhlProps),
        Map("/expenses/travelCosts/amount" ->  Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
      )
    }

    "reject over max boundary for rarReliefClaimed for FHL" in
    forAll(fhlR2TestData(rarReliefClaimed = 100000000000.00)) { fhlProps =>
      assertValidationErrorsWithCode[FHL.Properties](
        Json.toJson(fhlProps),
        Map("/expenses/rarReliefClaimed/amount" ->  Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
      )
    }

    "reject over max boundary for other for FHL (R1 boundary test)" in
      forAll(fhlR2TestData(other = BigDecimal("99999999999999.99"))) { fhlProps =>
        assertValidationErrorsWithCode[FHL.Properties](
          Json.toJson(fhlProps),
          Map("/expenses/other/amount" ->  Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
        )
      }
  }

  "accept max boundary R2 amounts for Other" in
    forAll(otherR2TestData()) { otherProps =>
      assertJsonValidationPasses[FHL.Properties](
        Json.toJson(otherProps)
      )
    }


  "reject over max boundary for rarRentReceived for Other" in
      forAll(otherR2TestData(rarRentReceived = 100000000000.00)) { otherProps =>
        assertValidationErrorsWithCode[Other.Properties](
          Json.toJson(otherProps),
          Map("/incomes/rarRentReceived" ->  Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
        )
      }

    "reject over max boundary for travelCosts for Other" in
      forAll(otherR2TestData(travelCosts = 100000000000.00)) { otherProps =>
        assertValidationErrorsWithCode[Other.Properties](
          Json.toJson(otherProps),
          Map("/expenses/travelCosts/amount" ->  Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
        )
      }

    "reject over max boundary for broughtFwdResidentialFinancialCost for Other" in
      forAll(otherR2TestData(broughtFwdResidentialFinancialCost = 100000000000.00)) { otherProps =>
        assertValidationErrorsWithCode[Other.Properties](
          Json.toJson(otherProps),
          Map("/expenses/broughtFwdResidentialFinancialCost/amount" ->  Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
        )
      }


  "reject over max boundary for rarReliefClaimed for Other" in
    forAll(otherR2TestData(rarReliefClaimed = 100000000000.00)) { otherProps =>
      assertValidationErrorsWithCode[Other.Properties](
        Json.toJson(otherProps),
        Map("/expenses/rarReliefClaimed/amount" ->  Seq(ErrorCode.INVALID_MONETARY_AMOUNT))
      )
    }







  def fhlR2TestData(rarRentReceived: BigDecimal = 99999999999.99, travelCosts: BigDecimal = 99999999999.99, rarReliefClaimed: BigDecimal = 99999999999.99, other: BigDecimal = 99999999999999.98) = FHL.Properties(
    id = None,
    from = LocalDate.parse("2019-04-30"),
    to = LocalDate.parse("2019-05-01"),
    financials = Some(
      FHL.Financials(
        incomes = Some(
          FHL.Incomes(
            rentIncome = Some(Income(2891, Some(754))),
            rarRentReceived = Some(Income(rarRentReceived, Some(0)))
          )
        ),
        expenses = Some(
          FHL.Expenses(
            premisesRunningCosts = Some(FHL.Expense(amount = 123)),
            repairsAndMaintenance = Some(FHL.Expense(amount = 123)),
            financialCosts = Some(FHL.Expense(amount = 123)),
            professionalFees = Some(FHL.Expense(amount = 123)),
            costOfServices = Some(FHL.Expense(amount = 123)),
            other = Some(FHL.Expense(other)),
            travelCosts = Some(FHL.Expense(travelCosts)),
            rarReliefClaimed = Some(FHL.Expense(rarReliefClaimed))
          )
        )
      )
    )
  )

  def otherR2TestData(rarRentReceived: BigDecimal = 99999999999.99, travelCosts: BigDecimal = 99999999999.99, broughtFwdResidentialFinancialCost: BigDecimal = 99999999999.99, rarReliefClaimed: BigDecimal = 99999999999.99) = Other.Properties(
    id = None,
    from = LocalDate.parse("2019-04-30"),
    to = LocalDate.parse("2019-05-01"),
    financials = Some(
      Other.Financials(
        incomes = Some(
          Other.Incomes(
            rentIncome = Some(Income(2891, Some(754))),
            premiumsOfLeaseGrant = Some(Income(323, Some(123))),
            reversePremiums = Some(Income(5466, Some(123))),
            otherPropertyIncome = Some(Income(64664, Some(123))),
            rarRentReceived = Some(Income(rarRentReceived, Some(0)))
          )
        ),
        expenses = Some(
          Other.Expenses(
            premisesRunningCosts = Some(Other.Expense(amount = 123)),
            repairsAndMaintenance = Some(Other.Expense(amount = 123)),
            financialCosts = Some(Other.Expense(amount = 123)),
            professionalFees = Some(Other.Expense(amount = 123)),
            costOfServices = Some(Other.Expense(amount = 123)),
            residentialFinancialCost = Some(Other.Expense(amount = 123)),
            other = Some(Other.Expense(amount = 123)),
            travelCosts = Some(Other.Expense(travelCosts)),
            broughtFwdResidentialFinancialCost = Some(Other.Expense(broughtFwdResidentialFinancialCost)),
            rarReliefClaimed = Some(Other.Expense(rarReliefClaimed))
          )
        )
      )
    )
  )

  val amount: Gen[BigDecimal] = amountGen(0, 5000)

  object FHLGen {
    val genIncome: Gen[Income] =
      for {
        amount <- amount
        taxDeducted <- Gen.option(amountGen(0, amount))

      } yield Income(amount, taxDeducted)

    val genRentIncome: Gen[Income] =
      for {
        amount <- amount
      } yield Income(amount, None)

    val genIncomes: Gen[FHL.Incomes] =
      for {
        rentIncome <- Gen.option(genIncome)
        rarRentReceived <- Gen.option(genRentIncome)
      } yield FHL.Incomes(rentIncome = rentIncome, rarRentReceived = rarRentReceived)

    val genExpense: Gen[FHL.Expense] = for (amount <- amount) yield FHL.Expense(amount)

    val genExpenses: Gen[FHL.Expenses] =
      for {
        premisesRunningCosts <- Gen.option(genExpense)
        repairsAndMaintenance <- Gen.option(genExpense)
        financialCosts <- Gen.option(genExpense)
        professionalFees <- Gen.option(genExpense)
        costOfServices <- Gen.option(genExpense)
        other <- Gen.option(genExpense)
        travelCosts <- Gen.option(genExpense)
        rarReliefClaimed <- Gen.option(genExpense)

      } yield
        FHL.Expenses(
          premisesRunningCosts = premisesRunningCosts,
          repairsAndMaintenance = repairsAndMaintenance,
          financialCosts = financialCosts,
          professionalFees = professionalFees,
          costOfServices = costOfServices,
          other = other,
          travelCosts = travelCosts,
          rarReliefClaimed = rarReliefClaimed
        )

    val genExpensesBoth: Gen[FHL.Expenses] =
      for {
        premisesRunningCosts <- Gen.option(genExpense)
        repairsAndMaintenance <- Gen.option(genExpense)
        financialCosts <- Gen.option(genExpense)
        professionalFees <- Gen.option(genExpense)
        costOfServices <- Gen.option(genExpense)
        consolidatedExpenses <- Gen.option(genExpense)
        other <- Gen.option(genExpense)
        travelCosts <- Gen.option(genExpense)
        rarReliefClaimed <- Gen.option(genExpense)
      } yield
        FHL.Expenses(
          premisesRunningCosts = premisesRunningCosts,
          repairsAndMaintenance = repairsAndMaintenance,
          financialCosts = financialCosts,
          professionalFees = professionalFees,
          consolidatedExpenses = consolidatedExpenses,
          other = other,
          travelCosts = travelCosts,
          rarReliefClaimed = rarReliefClaimed,
          costOfServices = costOfServices
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

  def amountGen(lower: BigDecimal, upper: BigDecimal): Gen[BigDecimal] =
    for (value <- Gen.chooseNum(lower.intValue(), upper.intValue())) yield BigDecimal(value)

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
        otherPropertyIncome <- Gen.option(genIncome)
        rarRentReceived <- Gen.option(genIncome)
      } yield Other.Incomes(rentIncome, premiumsOfLeaseGrant, reversePremiums, otherPropertyIncome, rarRentReceived)

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
        travelCosts <- Gen.option(genExpense)
        broughtFwdResidentialFinancialCost <- Gen.option(genExpense)
        rarReliefClaimed <- Gen.option(genExpense)
      } yield
        Other
          .Expenses(premisesRunningCosts = premisesRunningCosts,
            repairsAndMaintenance = repairsAndMaintenance,
            financialCosts = financialCosts,
            professionalFees = professionalFees,
            costOfServices = costOfServices,
            residentialFinancialCost = residentialFinancialCost,
            other = other,
            travelCosts = travelCosts,
            broughtFwdResidentialFinancialCost = broughtFwdResidentialFinancialCost,
            rarReliefClaimed = rarReliefClaimed)

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
        travelCosts <- Gen.option(genExpense)
        broughtFwdResidentialFinancialCost <- Gen.option(genExpense)
        rarReliefClaimed <- Gen.option(genExpense)
      } yield
        Other
          .Expenses(premisesRunningCosts,
            repairsAndMaintenance,
            financialCosts,
            professionalFees,
            costOfServices,
            consolidatedExpenses,
            residentialFinancialCost,
            other,
            travelCosts,
            broughtFwdResidentialFinancialCost,
            rarReliefClaimed)

    val genConsolidatedExpenses: Gen[Other.Expenses] =
      for {
        consolidatedExpenses <- Gen.option(genExpense)
        residentialFinancialCost <- Gen.option(genExpense)
        broughtFwdResidentialFinancialCost <- Gen.option(genExpense)
      } yield Other.Expenses(consolidatedExpenses = consolidatedExpenses, residentialFinancialCost = residentialFinancialCost,
        broughtFwdResidentialFinancialCost = broughtFwdResidentialFinancialCost)

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
