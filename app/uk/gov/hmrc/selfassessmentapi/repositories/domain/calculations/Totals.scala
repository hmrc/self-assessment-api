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

import uk.gov.hmrc.selfassessmentapi.controllers.api.SelfAssessment
import uk.gov.hmrc.selfassessmentapi.controllers.api.PositiveOrZero

object Totals {

  object IncomeReceived {
    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(NonSavings.TotalIncome(selfAssessment),
      Savings.TotalIncome(selfAssessment), Dividends.TotalIncome(selfAssessment))

    def apply(totalNonSavings: BigDecimal, totalSavings: BigDecimal, totalDividends: BigDecimal): BigDecimal =
      totalNonSavings + totalSavings + totalDividends
  }

  object TaxableIncome {
    def apply(selfAssessment: SelfAssessment): BigDecimal = apply(Totals.IncomeReceived(selfAssessment), Deductions.Total(selfAssessment))

    def apply(totalIncomeReceived: BigDecimal, totalDeduction: BigDecimal): BigDecimal = PositiveOrZero(totalIncomeReceived - totalDeduction)
  }

  object TaxDeducted {
    def apply(selfAssessment: SelfAssessment) = Savings.TotalTaxPaid(selfAssessment) + UKProperty.TotalTaxPaid(selfAssessment) + PensionSavingsCharges.TotalTaxPaid(selfAssessment)
  }

  object TaxDue {
    def apply(selfAssessment: SelfAssessment) = PositiveOrZero(
      Totals.IncomeTax(selfAssessment) + PensionSavingsCharges.IncomeTax(selfAssessment) - Totals.TaxDeducted(selfAssessment)
    )
  }

  object IncomeTax {
    def apply(selfAssessment: SelfAssessment) = NonSavings.IncomeTax(selfAssessment) + Savings.IncomeTax(selfAssessment) +
      Dividends.IncomeTax(selfAssessment)
  }

  object TaxOverpaid {
    def apply(selfAssessment: SelfAssessment) = PositiveOrZero(
      Totals.TaxDeducted(selfAssessment) - Totals.IncomeTax(selfAssessment) - PensionSavingsCharges.IncomeTax(selfAssessment))
  }

}
