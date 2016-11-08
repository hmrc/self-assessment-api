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

import uk.gov.hmrc.selfassessmentapi.controllers.api.{SelfAssessment, TaxBandSummary}
import uk.gov.hmrc.selfassessmentapi.controllers.api.{PositiveOrZero, TaxBandAllocation}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{IncomeTax, TaxBand}

object NonSavings {

  object TotalTaxableIncome {
    def apply(totalIncome: BigDecimal, totalDeductions: BigDecimal): BigDecimal =
      PositiveOrZero(totalIncome - totalDeductions)

    def apply(selfAssessment: SelfAssessment): BigDecimal =
      apply(TotalIncome(selfAssessment), Deductions.Total(selfAssessment))
  }

  object TotalIncome {
    def apply(selfEmploymentProfits: BigDecimal,
              ukPropertyProfits: BigDecimal,
              furnishedHolidayLettingProfits: BigDecimal): BigDecimal = {
      selfEmploymentProfits + ukPropertyProfits + furnishedHolidayLettingProfits
    }

    def apply(selfAssessment: SelfAssessment): BigDecimal =
      apply(SelfEmployment.TotalProfit(selfAssessment),
            UKProperty.TotalProfit(selfAssessment),
            FurnishedHolidayLetting.TotalProfit(selfAssessment))
  }

  object IncomeTaxBandSummary {
    def apply(selfAssessment: SelfAssessment): Seq[TaxBandSummary] =
      apply(NonSavings.TotalTaxableIncome(selfAssessment), Deductions.TotalUkPensionContributions(selfAssessment))

    private def apply(totalNonSavingsTaxableIncome: BigDecimal,
                      ukPensionContributions: BigDecimal = 0): Seq[TaxBandSummary] = {
      val basicTaxBand = TaxBand.BasicTaxBand(additionsToUpperBound = ukPensionContributions)
      val higherTaxBand = TaxBand.HigherTaxBand(basicTaxBand, additionsToUpperBound = ukPensionContributions)
      Seq(basicTaxBand, higherTaxBand, TaxBand.AdditionalHigherTaxBand(higherTaxBand)).map { taxBand =>
        TaxBandAllocation(taxBand.allocate2(totalNonSavingsTaxableIncome), taxBand).toTaxBandSummary
      }
    }
  }

  object IncomeTax extends IncomeTax {
    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(NonSavings.IncomeTaxBandSummary(selfAssessment))
    def apply(summaries: Seq[TaxBandSummary]): BigDecimal = incomeTax(summaries)
  }

}
