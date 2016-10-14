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

import uk.gov.hmrc.selfassessmentapi.controllers._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.bank.InterestType
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{SelfAssessment => _, _}

object Savings {

  object TotalTaxPaid {
    def apply(selfAssessment: SelfAssessment): BigDecimal = {
      selfAssessment.banks.map { bank =>
        RoundUpToPennies(TaxedInterest(bank) * 0.25)
      }.sum
    }
  }

  object TotalTaxableIncome {
    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(TotalIncome(selfAssessment), Deductions.Total(selfAssessment),
      NonSavings.TotalIncome(selfAssessment))
    def apply(totalSavingsIncome: BigDecimal, totalDeduction: BigDecimal, totalNonSavingsIncome: BigDecimal): BigDecimal = {
      PositiveOrZero(totalSavingsIncome - PositiveOrZero(totalDeduction - totalNonSavingsIncome))
    }
  }

  object TotalIncome {
    def apply(selfAssessment: SelfAssessment) = Incomes(selfAssessment).map(_.totalInterest).sum
  }

  private object Interest {
    def apply(bank: Bank, `type`: InterestType.Value) = bank.interests.filter(_.`type` == `type`).map(_.amount).sum
  }

  private object TaxedInterest {
    def apply(bank: Bank) = Interest(bank, InterestType.Taxed)
  }

  private object UntaxedInterest {
    def apply(bank: Bank) = Interest(bank, InterestType.Untaxed)
  }

  object Incomes {
    def apply(selfAssessment: SelfAssessment): Seq[InterestFromUKBanksAndBuildingSocieties] =
      selfAssessment.banks.map { bank =>
        new InterestFromUKBanksAndBuildingSocieties(bank.sourceId, RoundDown(TaxedInterest(bank) * 100/80 + UntaxedInterest(bank)))
      }
  }

  object StartingRate {
    private val startingRateLimit = BigDecimal(5000)
    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(NonSavings.TotalTaxableIncome(selfAssessment))
    def apply(totalNonSavingsTaxableIncome: BigDecimal): BigDecimal = PositiveOrZero(startingRateLimit - totalNonSavingsTaxableIncome)
  }

  object PersonalAllowance {
    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(Totals.TaxableIncome(selfAssessment), Deductions.TotalUkPensionContributions(selfAssessment))

    def apply(totalTaxableIncome: BigDecimal, ukPensionContributions: BigDecimal = 0): BigDecimal = {

      val basicBand = BasicTaxBand(additionsToUpperBound = ukPensionContributions)
      val higherBand = HigherTaxBand(basicBand, additionsToUpperBound = ukPensionContributions)

      totalTaxableIncome match {
        case total if total < 1 => 0
        case total if total isWithin basicBand  => 1000
        case total if total isWithin higherBand => 500
        case _ => 0
      }
     }
  }

  object IncomeTaxBandSummary {
    def apply(selfAssessment: SelfAssessment): Seq[TaxBandSummary] = apply(Savings.TotalTaxableIncome(selfAssessment),
      Savings.StartingRate(selfAssessment), Savings.PersonalAllowance(selfAssessment), NonSavings.TotalTaxableIncome(selfAssessment),
      Deductions.TotalUkPensionContributions(selfAssessment))

    private def apply(taxableSavingsIncome: BigDecimal, startingSavingsRate: BigDecimal, personalSavingsAllowance: BigDecimal,
              taxableNonSavingsIncome: BigDecimal, ukPensionContributions: BigDecimal = 0): Seq[TaxBandSummary] = {
      val startingTaxBand = TaxBand.SavingsStartingTaxBand(startingSavingsRate)
      val nilTaxBand = TaxBand.NilTaxBand(Some(startingTaxBand), personalSavingsAllowance)
      val basicTaxBand = TaxBand.BasicTaxBand(Some(nilTaxBand), reductionInUpperBound =  taxableNonSavingsIncome,
                                              additionsToUpperBound = ukPensionContributions)
      val higherTaxBand = TaxBand.HigherTaxBand(basicTaxBand, taxableNonSavingsIncome,
                                                additionsToUpperBound = ukPensionContributions)

      Seq(startingTaxBand, nilTaxBand, basicTaxBand, higherTaxBand, TaxBand.AdditionalHigherTaxBand(higherTaxBand)).map { taxBand =>
        api.TaxBandAllocation(taxBand.allocate2(taxableSavingsIncome), taxBand).toTaxBandSummary
      }
    }
  }

  object IncomeTax extends IncomeTax {
    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(Savings.IncomeTaxBandSummary(selfAssessment))
    def apply(taxBandSummaries: Seq[TaxBandSummary]): BigDecimal = incomeTax(taxBandSummaries)
  }

}


