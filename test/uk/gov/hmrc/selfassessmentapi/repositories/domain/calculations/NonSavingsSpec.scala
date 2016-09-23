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

class NonSavingsSpec extends UnitSpec {

  "NonSavings.TotalIncome" should {
    "be selfEmploymentProfits + employmentProfits + ukPropertyProfits + furnishedHolidayLettingProfits" in {
      NonSavings.TotalIncome(selfEmploymentProfits = 1000, employmentProfits = 2000, ukPropertyProfits = 500,
        furnishedHolidayLettingProfits = 200) shouldBe 3700
    }
  }

  "NonSavings.TaxBandSummaries" should {

    "be calculated for total NonSavingsIncome < 43000" in {

      NonSavings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(21000))
          .withUkProperties(UKPropertyBuilder().withRentIncomes(10000))
          .create()
      ) should contain theSameElementsAs
        Seq(
          TaxBandSummary("basicRate", 20000.00, "20%", 4000.00),
          TaxBandSummary("higherRate", 0.00, "40%", 0.00),
          TaxBandSummary("additionalHigherRate", 0.00, "45%", 0.00)
        )

      NonSavings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(32999))
          .withUkProperties(UKPropertyBuilder().withRentIncomes(10000))
          .create()
      ) should contain theSameElementsAs
        Seq(
          TaxBandSummary("basicRate", 31999.00, "20%", 6399.80),
          TaxBandSummary("higherRate", 0.0, "40%", 0.0),
          TaxBandSummary("additionalHigherRate", 0.0, "45%", 0.0)
        )
    }

    "be calculated for total NonSavingsIncome = 32000" in {
      NonSavings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(33000))
          .withUkProperties(UKPropertyBuilder().withRentIncomes(10000))
          .create()
      ) should contain theSameElementsAs
        Seq(
          TaxBandSummary("basicRate", 32000.00, "20%", 6400.00),
          TaxBandSummary("higherRate", 0, "40%", 0),
          TaxBandSummary("additionalHigherRate", 0, "45%", 0)
        )
    }

    "be calculated tax for 32000 < NonSavingsIncome < 150000" in {
      NonSavings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(33001))
          .withUkProperties(UKPropertyBuilder().withRentIncomes(10000))
          .create()
      ) should contain theSameElementsAs
        Seq(
          TaxBandSummary("basicRate", 32000.00, "20%", 6400.00),
          TaxBandSummary("higherRate", 1.0, "40%", 0.40),
          TaxBandSummary("additionalHigherRate", 0, "45%", 0)
        )

      NonSavings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(34003))
          .withUkProperties(UKPropertyBuilder().withRentIncomes(10000))
          .create()
      ) should contain theSameElementsAs
        Seq(
          TaxBandSummary("basicRate", 32000.00, "20%", 6400.00),
          TaxBandSummary("higherRate", 1003, "40%", 401.20),
          TaxBandSummary("additionalHigherRate", 0, "45%", 0)
        )
    }

    "be calculated tax for 32000 < NonSavingsIncome < 150000 and pension contributions" in {
      NonSavings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(33001))
          .withTaxYearProperties(TaxYearPropertiesBuilder().ukRegisteredPension(100))
          .withUkProperties(UKPropertyBuilder().withRentIncomes(10000))
          .create()
      ) should contain theSameElementsAs
        Seq(
          TaxBandSummary("basicRate", 32001.00, "20%", 6400.20),
          TaxBandSummary("higherRate", 0, "40%", 0),
          TaxBandSummary("additionalHigherRate", 0, "45%", 0)
        )
    }

    "be calculated tax for NonSavingsIncome = 150000" in {
      NonSavings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(140000))
          .withUkProperties(UKPropertyBuilder().withRentIncomes(10000))
          .create()
      ) should contain theSameElementsAs
        Seq(
          TaxBandSummary("basicRate", 32000.00, "20%", 6400.00),
          TaxBandSummary("higherRate", 118000.00, "40%", 47200.00),
          TaxBandSummary("additionalHigherRate", 0, "45%", 0)
        )
    }

    "be calculated tax for NonSavingsIncome > 150000" in {
      NonSavings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withEmployments(EmploymentBuilder().withSalary(10000))
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(120001))
          .withFurnishedHolidayLettings(FurnishedHolidayLettingBuilder().incomes(10000))
          .withUkProperties(UKPropertyBuilder().withRentIncomes(10000))
          .create()
      ) should contain theSameElementsAs
        Seq(
          TaxBandSummary("basicRate", 32000.00, "20%", 6400.00),
          TaxBandSummary("higherRate", 118000.00, "40%", 47200.00),
          TaxBandSummary("additionalHigherRate", 1, "45%", 0.45)
        )

    }

    "be calculated tax for NonSavingsIncome > 150000 and pension contributions" in {
      NonSavings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withEmployments(EmploymentBuilder().withSalary(10000))
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(120003))
          .withFurnishedHolidayLettings(FurnishedHolidayLettingBuilder().incomes(10000))
          .withUkProperties(UKPropertyBuilder().withRentIncomes(10000))
          .withTaxYearProperties(TaxYearPropertiesBuilder().ukRegisteredPension(100))
          .create()
      ) should contain theSameElementsAs
        Seq(
          TaxBandSummary("basicRate", 32100.00, "20%", 6420.00),
          TaxBandSummary("higherRate", 117903.00, "40%", 47161.20),
          TaxBandSummary("additionalHigherRate", 0, "45%", 0.00)
        )

      NonSavings.IncomeTaxBandSummary(
        SelfAssessmentBuilder()
          .withEmployments(EmploymentBuilder().withSalary(10000))
          .withSelfEmployments(SelfEmploymentBuilder().withTurnover(130000))
          .withFurnishedHolidayLettings(FurnishedHolidayLettingBuilder().incomes(10000))
          .withUkProperties(UKPropertyBuilder().withRentIncomes(10000))
          .withTaxYearProperties(TaxYearPropertiesBuilder().ukRegisteredPension(1000))
          .create()
      ) should contain theSameElementsAs
        Seq(
          TaxBandSummary("basicRate", 33000.00, "20%", 6600.00),
          TaxBandSummary("higherRate", 118000.00, "40%", 47200.00),
          TaxBandSummary("additionalHigherRate", 9000, "45%", 4050)
        )
    }

  }

  "NonSavingsIncomeTax" should {
    "be equal to" in {
      val inputs = Table(
        ("TotalTaxableProfits", "UkPensionContributions", "NonSavingsIncomeTax"),
        ("31001", "1000", "4000.20"),
        ("42999", "1000", "6399.80"),
        ("44001", "0", "6800.40"),
        ("44001", "1000", "6600.40"),
        ("100002", "0", "29201.20"),
        ("160003", "0", "58101.35"),
        ("160003", "10000", "55601.35")
      )
      TableDrivenPropertyChecks.forAll(inputs) { (totalTaxableProfits: String, ukPensionContributions: String, nonSavingsIncomeTax: String) =>
        NonSavings.IncomeTax(
          NonSavings.IncomeTaxBandSummary(
            SelfAssessmentBuilder()
              .withEmployments(EmploymentBuilder().withSalary(totalTaxableProfits.toInt))
              .withTaxYearProperties(TaxYearPropertiesBuilder().ukRegisteredPension(ukPensionContributions.toInt))
              .create()
          )
        ) shouldBe BigDecimal(nonSavingsIncomeTax.toDouble)
      }
    }
  }
}
