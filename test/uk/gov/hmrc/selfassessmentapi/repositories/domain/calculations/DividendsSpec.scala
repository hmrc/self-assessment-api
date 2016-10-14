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

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.prop.Tables.Table
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.{DividendsFromUKSources, SelfAssessment, TaxBandSummary}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.builders._

class DividendsSpec extends UnitSpec {

  "Dividends FromUK" should {

    "be empty when there are no dividends from uk sources" in {
      Dividends.FromUK(SelfAssessment()) shouldBe empty
    }

    "be calculated for multiple UK dividends from multiple income sources" in {
      val dividendsOne = DividendBuilder()
        .withUKDividends(1000.50)
        .withOtherUKDividends(2000.99)
        .create()


      val dividendsTwo = DividendBuilder()
        .withUKDividends(3000.50)
        .withOtherUKDividends(4000.999)
        .create()

      Dividends.FromUK(SelfAssessment(dividends = Seq(dividendsOne, dividendsTwo))) shouldBe
        Seq(DividendsFromUKSources(sourceId = dividendsOne.sourceId, BigDecimal(3001)),
          DividendsFromUKSources(sourceId = dividendsTwo.sourceId, BigDecimal(7001)))
    }

    "be calculated for a single one uk dividend for a single income source" in {
      val unearnedIncome = DividendBuilder()
        .withUKDividends(1000)
        .create()

      Dividends.FromUK(SelfAssessment(dividends = Seq(unearnedIncome))) shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncome.sourceId, BigDecimal(1000)))
    }

    "be calculated for multiple uk dividends from a single income source" in {
      val unearnedIncome = DividendBuilder()
        .withUKDividends(1000, 2000)
        .create()

      Dividends.FromUK(SelfAssessment(dividends = Seq(unearnedIncome))) shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncome.sourceId, BigDecimal(3000)))
    }

    "be round down to nearest pound for a single dividend" in {
      val unearnedIncome = DividendBuilder()
        .withUKDividends(1000.50)
        .create()

      Dividends.FromUK(SelfAssessment(dividends = Seq(unearnedIncome))) shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncome.sourceId, BigDecimal(1000)))
    }

    "be round down to nearest pound for multiple dividends" in {
      val unearnedIncome = DividendBuilder()
        .withUKDividends(1000.90, 2000.99)
        .create()

      Dividends.FromUK(SelfAssessment(dividends = Seq(unearnedIncome))) shouldBe
        Seq(DividendsFromUKSources(sourceId = unearnedIncome.sourceId, BigDecimal(3001)))
    }

  }

  "TotalDividends" should {
    "be sum of all dividends across multiple sources which have been rounded at source level" in {
      val unearnedIncomeOne = DividendBuilder()
        .withUKDividends(1000.50)
        .withOtherUKDividends(2000.99)
        .create()

      val unearnedIncomeTwo = DividendBuilder()
        .withUKDividends(3000.50)
        .withOtherUKDividends(4000.999)
        .create()

      Dividends.TotalIncome(SelfAssessment(dividends = Seq(unearnedIncomeOne, unearnedIncomeTwo))) shouldBe 10002
    }
  }

  "TaxableDividendIncome" should {
    "equal to total dividend income if there is no remaining deductions" in {
      Dividends.TotalTaxableIncome(totalDividendIncome = 1000, totalProfits = 11000, totalSavingsIncome = 0, totalDeduction = 11000) shouldBe 1000
    }

    "equal to total dividend income - deduction remain after allocation to profits and savings income" in {
      Dividends.TotalTaxableIncome(totalDividendIncome = 9001, totalProfits = 1000, totalSavingsIncome = 1000, totalDeduction = 11000) shouldBe 1
    }

    "equal to zero if total dividend income is equal to deduction remain after allocation to profits and savings income" in {
      Dividends.TotalTaxableIncome(totalDividendIncome = 9000, totalProfits = 1000, totalSavingsIncome = 1000, totalDeduction = 11000) shouldBe 0
    }

    "equal to zero if total dividend income is less than the deduction remain after allocation to profits and savings income" in {
      Dividends.TotalTaxableIncome(totalDividendIncome = 8999, totalProfits = 1000, totalSavingsIncome = 1000, totalDeduction = 11000) shouldBe 0
    }
  }

  "Dividends.IncomeTaxBandSummary" should {
    "be calculated when TaxableNonSavingsIncome = 0, TaxableSavingIncome = 0 and TaxableDividendsIncome falls within BasicRate band" in {
      Dividends.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withDividends(DividendBuilder().withUKDividends(42999))
          .create()
      ) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 26999.0, "7.5%", 2024.92),
          TaxBandSummary("higherRate", 0.0, "32.5%", 0.0),
          TaxBandSummary("additionalHigherRate", 0.0, "38.1%", 0.0)
        )
    }

    "be calculated when NonSavingsIncome = 0 and TaxableSavingIncome is spread over Basic and Higher Rate band" in {
      Dividends.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withDividends(DividendBuilder().withUKDividends(43001))
          .create()
      ) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 27000.0, "7.5%", 2025.0),
          TaxBandSummary("higherRate", 1.0, "32.5%", 0.32),
          TaxBandSummary("additionalHigherRate", 0.0, "38.1%", 0.0)
        )
    }

    "be calculated when NonSavingsIncome = 0, ukPensionContributions is present and TaxableSavingIncome is all in Basic Rate band" in {
      Dividends.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withTaxYearProperties(TaxYearPropertiesBuilder().withPensionContributions().ukRegisteredPension(500))
          .withDividends(DividendBuilder().withUKDividends(43001))
          .create()
      ) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 27001.0, "7.5%", 2025.07),
          TaxBandSummary("higherRate", 0, "32.5%", 0.0),
          TaxBandSummary("additionalHigherRate", 0.0, "38.1%", 0.0)
        )
    }

    "be calculated when NonSavingsIncome = 0 and TaxableSavingIncome is spread over Basic, Higher and Additional Higher Rate band" in {
      Dividends.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withDividends(DividendBuilder().withUKDividends(150001))
          .create()
      ) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 27000.0, "7.5%", 2025.0),
          TaxBandSummary("higherRate", 118000.0, "32.5%", 38350.0),
          TaxBandSummary("additionalHigherRate", 1.0, "38.1%", 0.38)
        )
    }

    "be calculated when NonSavingsIncome > 0 and TaxableSavingIncome falls within BasicRate band" in {
      Dividends.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(11250))
          .withUkProperties(UKPropertyBuilder().withRentIncomes(250))
          .withDividends(DividendBuilder().withUKDividends(30999))
          .withSavings(BankBuilder().withTaxedInterest(400))
          .create()
      ) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 25999.0, "7.5%", 1949.92),
          TaxBandSummary("higherRate", 0.0, "32.5%", 0.0),
          TaxBandSummary("additionalHigherRate", 0.0, "38.1%", 0.0)
        )
    }

    "be calculated when NonSavingsIncome > 0 and TaxableSavingIncome is spread over Basic and Higher Rate band" in {
      Dividends.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(11250))
          .withUkProperties(UKPropertyBuilder().withRentIncomes(250))
          .withDividends(DividendBuilder().withUKDividends(31001))
          .withSavings(BankBuilder().withTaxedInterest(400))
          .create()
      ) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 26000.0, "7.5%", 1950.0),
          TaxBandSummary("higherRate", 1.0, "32.5%", 0.32),
          TaxBandSummary("additionalHigherRate", 0.0, "38.1%", 0.0)
        )
    }

    "be calculated when NonSavingsIncome > 0 and TaxableSavingIncome is spread over Basic, Higher and Additional Higher Rate band" in {

      Dividends.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(250))
          .withUkProperties(UKPropertyBuilder().withRentIncomes(250))
          .withDividends(DividendBuilder().withUKDividends(149001))
          .withSavings(BankBuilder().withTaxedInterest(400))
          .create()
      ) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 26000.0, "7.5%", 1950.0),
          TaxBandSummary("higherRate", 118000.0, "32.5%", 38350.0),
          TaxBandSummary("additionalHigherRate", 1.0, "38.1%", 0.38)
        )
    }

   "be calculated when NonSavingsIncome > 0, ukPensionContributions is present and TaxableSavingIncome is spread over Basic and Higher Rate band" in {
     Dividends.IncomeTaxBandSummary(
       SelfAssessmentBuilder()
         .withSelfEmployments(SelfEmploymentBuilder().withTurnover(250))
         .withUkProperties(UKPropertyBuilder().withRentIncomes(250))
         .withTaxYearProperties(TaxYearPropertiesBuilder().withPensionContributions().ukRegisteredPension(500))
         .withDividends(DividendBuilder().withUKDividends(149001))
         .withSavings(BankBuilder().withTaxedInterest(400))
         .create()
     ) should contain theSameElementsInOrderAs
       Seq(
         TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
         TaxBandSummary("basicRate", 26500.0, "7.5%", 1987.5),
         TaxBandSummary("higherRate", 117501.0, "32.5%", 38187.82),
         TaxBandSummary("additionalHigherRate", 0.0, "38.1%", 0.0)
       )
    }

    "be calculated when selfEmploymentsIncome > 0, ukPropertiesIncome > 0 is in Basic Rate band" in {
      Dividends.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(5000))
          .withUkProperties(UKPropertyBuilder().withRentIncomes(5000))
          .withDividends(DividendBuilder().withUKDividends(18000))
          .create()
      ) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("nilRate", 5000.0, "0%", 0.0),
          TaxBandSummary("basicRate", 12000.0, "7.5%", 900),
          TaxBandSummary("higherRate", 0.0, "32.5%", 0.0),
          TaxBandSummary("additionalHigherRate", 0.0, "38.1%", 0.0)
        )
    }
  }

  "Dividends.IncomeTaxBandSummary" should {
    "acceptance test" in {
      val inputs = Table(
        ("TotalProfitFromSelfEmployments", "TotalSavingsIncome",  "UkPensionContribution", "TotalDividends",
          "NilRateAmount", "BasicRateTaxAmount", "HigherRateTaxAmount", "AdditionalHigherRateAmount"),
        ("0", "0", "0", "10999", "0", "0", "0", "0"),
        ("0", "0", "0", "11001", "1", "0", "0", "0"),
        ("0", "0", "0", "15000", "4000", "0", "0", "0"),
        ("0", "0", "0", "16001", "5000", "1", "0", "0"),
        ("0", "0", "0", "20000", "5000", "4000", "0", "0"),
        ("0", "0", "0", "33000", "5000", "17000", "0", "0"),
        ("0", "0", "0", "43000", "5000", "27000", "0", "0"),
        ("0", "0", "0", "43001", "5000", "27000", "1", "0"),
        ("0", "0", "0", "120000", "5000", "27000", "87000", "0"),
        ("0", "0", "0", "149000", "5000", "27000", "117000", "0"),
        ("0", "0", "5000", "149000", "5000", "32000", "112000", "0"),
        ("0", "0", "0", "150001", "5000", "27000", "118000", "1"),
        ("0", "0", "0", "151000", "5000", "27000", "118000", "1000"),
        ("1000", "0", "0", "11000", "1000", "0", "0", "0"),
        ("1000", "0", "0", "15000", "5000", "0", "0", "0"),
        ("1000", "0", "0", "16000", "5000", "1000", "0", "0"),
        ("11000", "0", "0", "5001", "5000", "1", "0", "0"),
        ("12000", "0", "0", "5000", "5000", "0", "0", "0"),
        ("12000", "0", "0", "6000", "5000", "1000", "0", "0"),
        ("12000", "0", "0", "139000", "5000", "15000", "118000", "1000"),
        ("12000", "0", "500", "139000", "5000", "15500", "118000", "500")
      )

      TableDrivenPropertyChecks.forAll(inputs) { (totalProfits: String, totalSavingsIncome: String, ukPensionContributions: String,
                                                  totalDividends:String, nilRateAmount: String, basicRateTaxAmount: String,
                                                  higherRateTaxAmount: String, additionalHigherRateAmount: String) =>

        val profits = BigDecimal(totalProfits.toInt)
        val savings = BigDecimal(totalSavingsIncome.toInt)
        val dividends = BigDecimal(totalDividends.toInt)
        val ukPensionsContributions = BigDecimal(ukPensionContributions.toInt)

        val dividendIncomeTax = Dividends.IncomeTaxBandSummary(
          SelfAssessmentBuilder()
            .withSelfEmployments(SelfEmploymentBuilder().withTurnover(profits))
            .withDividends(DividendBuilder().withUKDividends(dividends))
            .withSavings(BankBuilder().withUntaxedInterest(savings))
            .withTaxYearProperties(TaxYearPropertiesBuilder().withPensionContributions().ukRegisteredPension(ukPensionsContributions))
            .create()
        )

        println(dividendIncomeTax.map(_.taxableAmount))
        println("==========================================")

        dividendIncomeTax.map(_.taxableAmount) should contain theSameElementsInOrderAs
          Seq(BigDecimal(nilRateAmount.toInt), BigDecimal(basicRateTaxAmount.toInt), BigDecimal(higherRateTaxAmount.toInt), BigDecimal(additionalHigherRateAmount.toInt))
      }
    }
  }

  "Dividends.PersonalAllowance" should {

    "acceptance test" in {

      val inputs = Table(
        ("ProfitFromSelfEmployment", "SavingsIncome", "DividendIncome", "PersonalDividendAllowance"),
        ("8000", "0", "2000", "0"),
        ("8000", "0", "6000", "1000"),
        ("8000", "2000", "6000", "3000"),
        ("11000", "2000", "4999", "4999"),
        ("11000", "5000", "5001", "5000"),
        ("13000", "2000", "8000", "5000"),
        ("15000", "3000", "10000", "5000"),
        ("13000", "0", "2000", "2000"),
        ("11000", "0", "2000", "0"),
        ("13000", "0", "5000", "5000"),
        ("14000", "0", "6000", "5000"),
        ("16000", "3000", "8000", "5000")
      )

      TableDrivenPropertyChecks.forAll(inputs) { (profitFromSelfEmployment: String, savingsIncome: String, dividendIncome: String,
                                                  personalDividendAllowance: String) =>

        val profits = Print(BigDecimal(profitFromSelfEmployment.toInt)).as("profits")
        val savings = Print(BigDecimal(savingsIncome.toInt)).as("savings")
        val dividends = Print(BigDecimal(dividendIncome.toInt)).as("dividends")

        val totalIncome = Print(profits + savings + dividends).as("totalIncome")
        val totalDeduction = Print(Deductions.Total(2000, Deductions.PersonalAllowance(totalIncome, 0, 0), 0)).as("totalDeduction")
        val taxableDividendIncome = Print(Dividends.TotalTaxableIncome(dividends, profits, savings, totalDeduction)).as("taxableDividendIncome")

        println("=======================================")

        Dividends.PersonalAllowance(taxableDividendIncome) shouldBe personalDividendAllowance.toInt
      }
    }

  }
}
