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

import uk.gov.hmrc.selfassessmentapi.controllers.api.{RoundUp, SelfAssessment, TaxBandAllocation, TaxBandSummary}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{IncomeTax, TaxBand}

object PensionSavingsCharges {


  private def getPensionSavings(selfAssessment: SelfAssessment) = {
    for {
      taxYearProperties <- selfAssessment.taxYearProperties
      pensionContribution <- taxYearProperties.pensionContributions
      pensionSavings <- pensionContribution.pensionSavings
    } yield pensionSavings
  }

  object TotalTaxPaid {
    def apply(selfAssessment: SelfAssessment): BigDecimal = {
      getPensionSavings(selfAssessment).flatMap( _.taxPaidByPensionScheme).getOrElse(BigDecimal(0))
    }
  }

  object IncomeTaxBandSummary  {

    private def apply(totalTaxableIncome: BigDecimal, ukPensionContribution: BigDecimal = 0, pensionContributionExcess: BigDecimal): Seq[TaxBandSummary] = {
      val basicTaxBand = TaxBand.BasicTaxBand(reductionInUpperBound = totalTaxableIncome, additionsToUpperBound = ukPensionContribution)
      val higherTaxBand = TaxBand.HigherTaxBand(basicTaxBand, totalTaxableIncome, ukPensionContribution)
      Seq(basicTaxBand, higherTaxBand, TaxBand.AdditionalHigherTaxBand(higherTaxBand)).map { taxBand =>
        TaxBandAllocation(taxBand.allocate2(RoundUp(pensionContributionExcess)), taxBand).toTaxBandSummary
      }
    }

    def apply(selfAssessment: SelfAssessment): Seq[TaxBandSummary] = {
      apply(NonSavings.TotalTaxableIncome(selfAssessment) + Savings.TotalTaxableIncome(selfAssessment) +
            Dividends.TotalTaxableIncome(selfAssessment),
        Deductions.TotalUkPensionContributions(selfAssessment),
        getPensionSavings(selfAssessment).flatMap(_.excessOfAnnualAllowance).getOrElse(BigDecimal(0)))
    }
  }

  object IncomeTax extends IncomeTax {
    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(IncomeTaxBandSummary(selfAssessment))
    def apply(taxBandSummaries: Seq[TaxBandSummary]): BigDecimal = incomeTax(taxBandSummaries)
  }

}




