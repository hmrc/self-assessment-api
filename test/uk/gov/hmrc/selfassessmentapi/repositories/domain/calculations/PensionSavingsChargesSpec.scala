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
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxBandSummary
import uk.gov.hmrc.selfassessmentapi.repositories.domain.builders._

class PensionSavingsChargesSpec extends UnitSpec {

  "PensionSavingsCharges.IncomeTaxBandSummary" should {
    "be calculated when TotalTaxableIncome present falls within HigherRate band" in {
      PensionSavingsCharges.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSavings(BankBuilder().withUntaxedInterest(30999))
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(12000))
          .withTaxYearProperties(
            TaxYearPropertiesBuilder().withPensionContributions().pensionSavings(excessOfAnnualAllowance = 2000))
          .create()) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("basicRate", 1, "20%", 0.20),
          TaxBandSummary("higherRate", 1999, "40%", 799.60),
          TaxBandSummary("additionalHigherRate", 0.0, "45%", 0.0)
        )
    }

    "be calculated when TotalTaxableIncome and ukPensionsContributions present falls within Basic and Higher rate band" in {
      PensionSavingsCharges.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSavings(BankBuilder().withUntaxedInterest(30999))
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(6000))
          .withTaxYearProperties(TaxYearPropertiesBuilder()
            .withPensionContributions()
            .ukRegisteredPension(1500)
            .pensionSavings(excessOfAnnualAllowance = 2000))
          .create()) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("basicRate", 2000.00, "20%", 400.00),
          TaxBandSummary("higherRate", 0.00, "40%", 0.00),
          TaxBandSummary("additionalHigherRate", 0.00, "45%", 0.00)
        )
    }

    "be calculated when TotalTaxableIncome and ukPensionsContributions present falls within Basic, Higher and Additional Higher rate band" in {
      PensionSavingsCharges.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSavings(BankBuilder().withUntaxedInterest(31999))
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(5000))
          .withTaxYearProperties(TaxYearPropertiesBuilder()
            .withPensionContributions()
            .ukRegisteredPension(40000)
            .pensionSavings(excessOfAnnualAllowance = 200000))
          .create()) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("basicRate", 46001.00, "20%", 9200.20),
          TaxBandSummary("higherRate", 118000.00, "40%", 47200.00),
          TaxBandSummary("additionalHigherRate", 35999.00, "45%", 16199.55)
        )
    }

    "round up the Pension Contribution Excess to the nearest pound" in {
      PensionSavingsCharges.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(12000))
          .withTaxYearProperties(
            TaxYearPropertiesBuilder()
              .withPensionContributions()
              .ukRegisteredPension(40000)
              .pensionSavings(excessOfAnnualAllowance = 200000.01))
          .create()) should contain theSameElementsInOrderAs
        Seq(
          TaxBandSummary("basicRate", 71000.00, "20%", 14200.00),
          TaxBandSummary("higherRate", 118000.00, "40%", 47200.00),
          TaxBandSummary("additionalHigherRate", 11001.00, "45%", 4950.45)
        )
    }
  }

  "PensionSavingsCharges.PersonalAllowance" should {

    "acceptance test" in {

      val inputs = Table(
        ("TotalTaxableIncome", "UkPensionContribution", "PensionContributionExcess", "IncomeTaxCalculated"),
        ("8000", "0", "2000", "400"),
        ("8000", "0", "6000", "1200"),
        ("13000", "0", "2000", "400"),
        ("13000", "0", "5000", "1000"),
        ("14000", "0", "6000", "1200"),
        ("8000", "2000", "6000", "1200"),
        ("11000", "2000", "4999", "999.80"),
        ("11000", "5000", "5001", "1000.20"),
        ("13000", "2000", "8000", "1600"),
        ("15000", "3000", "10000", "2000"),
        ("16000", "3000", "8000", "1600")
      )

      TableDrivenPropertyChecks.forAll(inputs) {
        (totalTaxableIncome: String, pensionContribution: String, pensionContributionExcess: String,
         incomeTaxCalculated: String) =>
          val totalTaxables = Print(BigDecimal(totalTaxableIncome.toInt)).as("totalTaxableIncome")
          val contribution = Print(BigDecimal(pensionContribution.toInt)).as("ukPensionContribution")
          val contributionExcess = Print(BigDecimal(pensionContributionExcess.toInt)).as("pensionContributionExcess")

          val taxBands = PensionSavingsCharges.IncomeTaxBandSummary(
            SelfAssessmentBuilder()
              .withTaxYearProperties(
                TaxYearPropertiesBuilder()
                  .withPensionContributions()
                  .ukRegisteredPension(contribution)
                  .pensionSavings(excessOfAnnualAllowance = contributionExcess))
              .create())

          PensionSavingsCharges.IncomeTax(taxBands) shouldBe BigDecimal(incomeTaxCalculated)

          println("=======================================")
      }
    }

  }
}
