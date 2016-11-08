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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.calculations

import org.scalacheck.Gen
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.prop.Tables.Table
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api
import uk.gov.hmrc.selfassessmentapi.controllers.api.{
  InterestFromUKBanksAndBuildingSocieties,
  SelfAssessment,
  TaxBandSummary
}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.builders._

class SavingsSpec extends UnitSpec {

  "Interest from UK banks and building societies" should {

    "calculate rounded down interest when there are multiple interest of both taxed and unTaxed from uk banks and building societies from" +
      " multiple benefit sources" in {

      val banksOne = BankBuilder().withTaxedInterest(100.50).withUntaxedInterest(200.50).create()

      val banksTwo = BankBuilder().withTaxedInterest(300.99).withUntaxedInterest(400.99).create()

      Savings.Incomes(SelfAssessment(banks = Seq(banksOne, banksTwo))) should contain theSameElementsAs
        Seq(api.InterestFromUKBanksAndBuildingSocieties(sourceId = banksOne.sourceId, BigDecimal(326)),
            api.InterestFromUKBanksAndBuildingSocieties(sourceId = banksTwo.sourceId, BigDecimal(777)))
    }

    "calculate interest when there is one taxed interest from uk banks and building societies from a single benefit source" in {
      val bank = BankBuilder().withTaxedInterest(100).create()

      Savings.Incomes(SelfAssessment(banks = Seq(bank))) should contain theSameElementsAs
        Seq(api.InterestFromUKBanksAndBuildingSocieties(bank.sourceId, BigDecimal(125)))

    }

    "calculate interest when there are multiple taxed interest from uk banks and building societies from a single benefit source" in {
      val bank = BankBuilder().withTaxedInterest(100, 200).create()

      Savings.Incomes(SelfAssessment(banks = Seq(bank))) should contain theSameElementsAs
        Seq(api.InterestFromUKBanksAndBuildingSocieties(bank.sourceId, BigDecimal(375)))
    }

    "calculate round down interest when there is one taxed interest from uk banks and building societies from a single benefit source income " +
      "source" in {
      val bank = BankBuilder().withTaxedInterest(100.50).create()

      Savings.Incomes(SelfAssessment(banks = Seq(bank))) should contain theSameElementsAs
        Seq(api.InterestFromUKBanksAndBuildingSocieties(bank.sourceId, BigDecimal(125)))
    }

    "calculate round down interest when there are multiple taxed interest from uk banks and building societies from a single bank source" in {
      val bank = BankBuilder().withTaxedInterest(100.90, 200.99).create()

      Savings.Incomes(SelfAssessment(banks = Seq(bank))) should contain theSameElementsAs
        Seq(api.InterestFromUKBanksAndBuildingSocieties(bank.sourceId, BigDecimal(377)))

    }

    "calculate interest when there is one unTaxed interest from uk banks and building societies from a single bank source" in {
      val bank = BankBuilder().withUntaxedInterest(100).create()

      Savings.Incomes(SelfAssessment(banks = Seq(bank))) should contain theSameElementsAs
        Seq(InterestFromUKBanksAndBuildingSocieties(bank.sourceId, BigDecimal(100)))

    }

    "calculate interest when there are multiple unTaxed interest from uk banks and building societies" in {
      val bank = BankBuilder().withUntaxedInterest(100, 200).create()

      Savings.Incomes(SelfAssessment(banks = Seq(bank))) should contain theSameElementsAs
        Seq(api.InterestFromUKBanksAndBuildingSocieties(bank.sourceId, BigDecimal(300)))
    }

    "calculate rounded down interest when there is one unTaxed interest from uk banks and building societies" in {
      val bank = BankBuilder().withUntaxedInterest(100.50).create()

      Savings.Incomes(SelfAssessment(banks = Seq(bank))) should contain theSameElementsAs
        Seq(api.InterestFromUKBanksAndBuildingSocieties(bank.sourceId, BigDecimal(100)))
    }

    "calculate rounded down interest when there are multiple unTaxed interest from uk banks and building societies" in {
      val bank = BankBuilder().withUntaxedInterest(100.50, 200.99).create()

      Savings.Incomes(SelfAssessment(banks = Seq(bank))) should contain theSameElementsAs
        Seq(api.InterestFromUKBanksAndBuildingSocieties(bank.sourceId, BigDecimal(301)))
    }
  }

  "SavingsStartingRate" should {

    "be 5000 when totalNonSavingsTaxableIncome is 0" in {
      Savings.StartingRate(totalNonSavingsTaxableIncome = 0) shouldBe 5000
    }

    "be the 5000 - totalNonSavingsTaxableIncome when totalNonSavingsTaxableIncome < 5000" in {
      Savings.StartingRate(totalNonSavingsTaxableIncome = 3000) shouldBe 2000
      Savings.StartingRate(totalNonSavingsTaxableIncome = 4999) shouldBe 1
    }

    "be 0 when totalNonSavingsTaxableIncome == 5000" in {
      Savings.StartingRate(totalNonSavingsTaxableIncome = 5000) shouldBe 0
    }

    "be 0 when totalNonSavingsTaxableIncome > 5000" in {
      Savings.StartingRate(totalNonSavingsTaxableIncome = 5001) shouldBe 0
    }
  }

  "Savings.PersonalSavingsAllowance" should {
    def generate(lowerLimit: Int, upperLimit: Int) = for { value <- Gen.chooseNum(lowerLimit, upperLimit) } yield value

    "be zero when the total income on which tax is due is zero" in {
      Savings.PersonalAllowance(totalTaxableIncome = 0) shouldBe 0
    }

    "be 1000 when the total income on which tax is due is less than equal to 32000 " in {
      Savings.PersonalAllowance(totalTaxableIncome = 1) shouldBe 1000
      generate(1, 32000) map { randomNumber =>
        Savings.PersonalAllowance(totalTaxableIncome = randomNumber) shouldBe 1000
      }
      Savings.PersonalAllowance(totalTaxableIncome = 32000) shouldBe 1000
    }

    "be 1000 when the total income on which tax is due is greater than 32000 and ukPensionContributions is present" in {
      generate(1, 35000) map { randomNumber =>
        Savings.PersonalAllowance(totalTaxableIncome = randomNumber, ukPensionContributions = 3500) shouldBe 1000
      }
      Savings.PersonalAllowance(totalTaxableIncome = 33000, ukPensionContributions = 3000) shouldBe 1000
    }

    "be 500 when the total income on which tax is due is greater than 150000 but less than 150000 + ukPensionContributions " in {
      generate(150001, 153000) map { randomNumber =>
        Savings.PersonalAllowance(totalTaxableIncome = randomNumber, ukPensionContributions = 3500) shouldBe 500
      }
    }

    "be 500 when the total income on which tax is due is greater than 32000 but less than equal to 150000" in {
      Savings.PersonalAllowance(totalTaxableIncome = 32001) shouldBe 500
      generate(32001, 150000) map { randomNumber =>
        Savings.PersonalAllowance(totalTaxableIncome = randomNumber) shouldBe 500
      }
      Savings.PersonalAllowance(totalTaxableIncome = 150000) shouldBe 500
    }

    "be 0 when the total income on which tax is due is greater than 150000" in {
      Savings.PersonalAllowance(totalTaxableIncome = 150001) shouldBe 0
      generate(150001, Int.MaxValue) map { randomNumber =>
        Savings.PersonalAllowance(totalTaxableIncome = randomNumber) shouldBe 0
      }
    }
  }

  "Savings.TotalTaxableIncome" should {
    "be equal to TotalSavingsIncomes - ((PersonalAllowance + IncomeTaxRelief) - ProfitsFromSelfEmployments) if ProfitsFromSelfEmployments" +
      " < (PersonalAllowance + IncomeTaxRelief) " in {
      Savings
        .TotalTaxableIncome(totalSavingsIncome = 5000, totalDeduction = 4000, totalNonSavingsIncome = 2000) shouldBe 3000
      Savings
        .TotalTaxableIncome(totalSavingsIncome = 5000, totalDeduction = 4000, totalNonSavingsIncome = 3999) shouldBe 4999
    }

    "be equal to TotalSavingsIncomes if ProfitsFromSelfEmployments >= (PersonalAllowance + IncomeTaxRelief) " in {
      Savings
        .TotalTaxableIncome(totalSavingsIncome = 5000, totalDeduction = 4000, totalNonSavingsIncome = 4000) shouldBe 5000
      Savings
        .TotalTaxableIncome(totalSavingsIncome = 5000, totalDeduction = 4000, totalNonSavingsIncome = 4001) shouldBe 5000
      Savings
        .TotalTaxableIncome(totalSavingsIncome = 5000, totalDeduction = 4000, totalNonSavingsIncome = 4500) shouldBe 5000
    }
  }

  "Savings.TotalTaxPaid" should {

    "be 0 when there is no interest from banks" in {
      Savings.TotalTaxPaid(SelfAssessment()) shouldBe 0
    }

    "be 0 if sum of all taxed interests is 0" in {

      val saving = BankBuilder().withTaxedInterest(0, 0).withUntaxedInterest(0).create()

      Savings.TotalTaxPaid(SelfAssessment(banks = Seq(saving))) shouldBe 0
    }

    "be equal to Sum(Taxed Interest) * 100/80 - Sum(Taxed Interest)" in {

      val savingOne = BankBuilder().withTaxedInterest(100, 200, 2000).withUntaxedInterest(500).create()

      Savings.TotalTaxPaid(SelfAssessment(banks = Seq(savingOne))) shouldBe 575

      val savingTwo = BankBuilder().withTaxedInterest(400, 700, 5800).withUntaxedInterest(500).create()

      Savings.TotalTaxPaid(SelfAssessment(banks = Seq(savingTwo))) shouldBe 1725

    }

    "be equal to RoundUpToPennies(RoundUp(Sum(Taxed Interest)) * 100/80 - Sum(Taxed Interest))" in {

      val savingOne = BankBuilder().withTaxedInterest(786.78, 456.76, 2000.56).withUntaxedInterest(1000.56).create()

      Savings.TotalTaxPaid(SelfAssessment(banks = Seq(savingOne))) shouldBe 811.03

      val savingTwo = BankBuilder().withTaxedInterest(1000.78, 999.22, 3623.67).withUntaxedInterest(2000.56).create()

      Savings.TotalTaxPaid(SelfAssessment(banks = Seq(savingTwo))) shouldBe 1405.92
    }

    "be equal to RoundUpToPennies(RoundUp(Sum(Taxed Interest)) * 100/80 - Sum(Taxed Interest)) for multiple bank sources" in {
      val savingOne = BankBuilder().withTaxedInterest(786.78).withUntaxedInterest(2500.00).create()

      val savingTwo = BankBuilder().withTaxedInterest(456.76, 2000.56).withUntaxedInterest(2500.00).create()

      Savings.TotalTaxPaid(SelfAssessment(banks = Seq(savingOne, savingTwo))) shouldBe 811.03
    }
  }

  "Savings.IncomeTaxBandSummary" should {
    "be calculated when NonSavingsIncome = 0 and TaxableSavingIncome falls within BasicRate band" in {
      Savings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSavings(BankBuilder().withUntaxedInterest(42999))
          .create()) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("startingRate", 5000.0, "0%", 0.0),
          TaxBandSummary("nilRate", 1000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 25999.0, "20%", 5199.80),
          TaxBandSummary("higherRate", 0.0, "40%", 0.0),
          TaxBandSummary("additionalHigherRate", 0.0, "45%", 0.0)
        )
    }

    "be calculated when NonSavingsIncome = 0 and TaxableSavingIncome is spread over Basic and Higher Rate band" in {
      Savings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSavings(BankBuilder().withUntaxedInterest(43001))
          .create()) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("startingRate", 5000.0, "0%", 0.0),
          TaxBandSummary("nilRate", 500.00, "0%", 0.0),
          TaxBandSummary("basicRate", 26500.0, "20%", 5300.0),
          TaxBandSummary("higherRate", 1.0, "40%", 0.4),
          TaxBandSummary("additionalHigherRate", 0.0, "45%", 0.0)
        )
    }

    "be calculated when NonSavingsIncome = 0 and TaxableSavingIncome is spread over Basic, Higher and Additional Higher Rate band" in {
      Savings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSavings(BankBuilder().withUntaxedInterest(150001))
          .create()) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("startingRate", 5000.0, "0%", 0.0),
          TaxBandSummary("nilRate", 0.0, "0%", 0.0),
          TaxBandSummary("basicRate", 27000.0, "20%", 5400.0),
          TaxBandSummary("higherRate", 118000.0, "40%", 47200.0),
          TaxBandSummary("additionalHigherRate", 1.0, "45%", 0.45)
        )
    }

    "be calculated when NonSavingsIncome > 0 and TaxableSavingIncome falls within BasicRate band" in {

      Savings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSavings(BankBuilder().withUntaxedInterest(30999))
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(5500))
          .create()) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("startingRate", 5000.0, "0%", 0.0),
          TaxBandSummary("nilRate", 1000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 19499.0, "20%", 3899.8),
          TaxBandSummary("higherRate", 0.0, "40%", 0.0),
          TaxBandSummary("additionalHigherRate", 0.0, "45%", 0.0)
        )
    }

    "be calculated when NonSavingsIncome > 0 and TaxableSavingIncome is spread over Basic and Higher Rate band" in {
      Savings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSavings(BankBuilder().withUntaxedInterest(31001))
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(6000))
          .create()) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("startingRate", 5000.0, "0%", 0.0),
          TaxBandSummary("nilRate", 1000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 20001.0, "20%", 4000.20),
          TaxBandSummary("higherRate", 0.0, "40%", 0.0),
          TaxBandSummary("additionalHigherRate", 0.0, "45%", 0.0)
        )
    }

    "be calculated when NonSavingsIncome > 0 and TaxableSavingIncome is spread over Basic, Higher and Additional Higher Rate band" in {

      Savings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSavings(BankBuilder().withUntaxedInterest(149001))
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(500))
          .create()) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("startingRate", 4500.00, "0%", 0.00),
          TaxBandSummary("nilRate", 500.00, "0%", 0.00),
          TaxBandSummary("basicRate", 26500.00, "20%", 5300.00),
          TaxBandSummary("higherRate", 117501.00, "40%", 47000.40),
          TaxBandSummary("additionalHigherRate", 0.00, "45%", 0.00)
        )
    }

    "be calculated when NonSavingsIncome > 0 and TaxableSavingIncome is spread over Basic, Higher and Additional Higher Rate band and ukPensionContributions are present" in {
      Savings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSavings(BankBuilder().withUntaxedInterest(149001))
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(500))
          .withTaxYearProperties(TaxYearPropertiesBuilder().withPensionContributions().ukRegisteredPension(500))
          .create()) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("startingRate", 4500.00, "0%", 0.00),
          TaxBandSummary("nilRate", 500.00, "0%", 0.00),
          TaxBandSummary("basicRate", 27000.00, "20%", 5400.00),
          TaxBandSummary("higherRate", 117001.00, "40%", 46800.40),
          TaxBandSummary("additionalHigherRate", 0.00, "45%", 0.00)
        )
    }
  }

  "Savings.IncomeTaxBandSummary" should {
    "be allocated to correct tax bands" in {
      val inputs = Table(
        ("TotalProfitFromSelfEmployments",
         "TotalSavingsIncome",
         "UkPensionContribution",
         "StartingRateAmount",
         "NilRateAmount",
         "BasicRateTaxAmount",
         "HigherRateTaxAmount",
         "AdditionalHigherRateAmount"),
        ("8000", "12000", "0", "5000", "1000", "3000", "0", "0"),
        ("5000", "6000", "0", "0", "0", "0", "0", "0"),
        ("5000", "7000", "0", "1000", "0", "0", "0", "0"),
        ("5000", "7000", "100", "1000", "0", "0", "0", "0"),
        ("5000", "11000", "0", "5000", "0", "0", "0", "0"),
        ("5000", "12000", "0", "5000", "1000", "0", "0", "0"),
        ("20000", "11000", "0", "0", "1000", "10000", "0", "0"),
        ("20000", "11000", "1000", "0", "1000", "10000", "0", "0"),
        ("29000", "12000", "0", "0", "1000", "11000", "0", "0"),
        ("32000", "12000", "0", "0", "500", "10500", "1000", "0"),
        ("32000", "12000", "500", "0", "500", "11000", "500", "0"),
        ("100000", "12000", "0", "0", "500", "0", "11500", "0"),
        ("140000", "12000", "0", "0", "0", "0", "10000", "2000"),
        ("140000", "12000", "5000", "0", "500", "0", "11500", "0"),
        ("150000", "12000", "0", "0", "0", "0", "0", "12000"),
        ("60000", "85000", "0", "0", "500", "0", "84500", "0"),
        ("80000", "85000", "0", "0", "0", "0", "70000", "15000"),
        ("80000", "85000", "10000", "0", "0", "0", "80000", "5000"),
        ("13000", "7000", "0", "3000", "1000", "3000", "0", "0"),
        ("14000", "8000", "0", "2000", "1000", "5000", "0", "0"),
        ("14000", "8000", "5000", "2000", "1000", "5000", "0", "0")
      )

      TableDrivenPropertyChecks.forAll(inputs) {
        (totalProfitFromSelfEmployments: String, totalSavingsIncome: String, ukPensionContributions: String,
         startingRateAmount: String, nilRateAmount: String, basicRateTaxAmount: String, higherRateTaxAmount: String,
         additionalHigherRateAmount: String) =>
          val bandAllocations = Savings.IncomeTaxBandSummary(SelfAssessmentBuilder()
            .withSavings(BankBuilder().withUntaxedInterest(totalSavingsIncome.toInt))
            .withSelfEmployments(SelfEmploymentBuilder().withTurnover(totalProfitFromSelfEmployments.toInt))
            .withTaxYearProperties(
              TaxYearPropertiesBuilder().withPensionContributions().ukRegisteredPension(ukPensionContributions.toInt))
            .create())

          println(bandAllocations)
          println("====================================================================================")

          bandAllocations.map(_.taxableAmount) should contain theSameElementsInOrderAs Seq(
            startingRateAmount.toInt,
            nilRateAmount.toInt,
            basicRateTaxAmount.toInt,
            higherRateTaxAmount.toInt,
            additionalHigherRateAmount.toInt)
      }
    }
  }

  "SavingsIncomeTax" should {
    "be equal to" in {
      val inputs = Table(
        ("NonSavingsIncome", "SavingsIncome", "UkPensionContribution", "SavingsIncomeTax"),
        ("0", "12000", "0", "0"),
        ("0", "17001", "0", "0.2"),
        ("0", "17005", "0", "1"),
        ("0", "20000", "0", "600"),
        ("0", "43000", "0", "5200"),
        ("0", "43001", "0", "5300.4"),
        ("0", "43005", "0", "5302"),
        ("0", "100000", "0", "28100"),
        ("0", "100000", "10000", "26100"),
        ("0", "150000", "0", "52500"),
        ("0", "150001", "0", "52600.45"),
        ("0", "150005", "0", "52602.25"),
        ("0", "160000", "0", "57100"),
        ("11000", "32000", "0", "5200"),
        ("11000", "32001", "0", "5300.4"),
        ("11000", "32005", "0", "5302"),
        ("11000", "89000", "0", "28100"),
        ("11000", "150000", "0", "56350"),
        ("11000", "150001", "0", "56350.45"),
        ("11000", "150001", "20000", "51700.40"),
        ("11000", "150005", "0", "56352.25"),
        ("11000", "160000", "0", "60850"),
        ("11000", "160000", "30000", "53700")
      )

      TableDrivenPropertyChecks.forAll(inputs) {
        (nonSavingsIncome: String, savingsIncome: String, ukPensionContributions: String, savingsIncomeTax: String) =>
          val nonSavings = BigDecimal(nonSavingsIncome.toInt)
          val savings = BigDecimal(savingsIncome.toInt)
          val ukPensionContribs = BigDecimal(ukPensionContributions.toInt)

          Print(savings).as("savings")
          Print(nonSavings).as("nonSavings")
          Print(ukPensionContribs).as("ukPensionContributions")

          val savingsIncomeBandAllocation = Savings.IncomeTaxBandSummary(
            SelfAssessmentBuilder()
              .withSavings(BankBuilder().withUntaxedInterest(savings))
              .withSelfEmployments(SelfEmploymentBuilder().withTurnover(nonSavings))
              .withTaxYearProperties(
                TaxYearPropertiesBuilder().withPensionContributions().ukRegisteredPension(ukPensionContribs))
              .create())

          println(savingsIncomeBandAllocation)
          println("==============================")

          Savings.IncomeTax(savingsIncomeBandAllocation) shouldBe BigDecimal(savingsIncomeTax.toDouble)
      }
    }
  }

}
