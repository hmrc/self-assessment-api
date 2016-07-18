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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps

import uk.gov.hmrc.selfassessmentapi.domain.IncomeTaxDeducted
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SavingsIncomeType._
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec}

class TaxDeductedAmountForUkSavingsIncomeCalculationSpec extends UnitSpec with SelfEmploymentSugar {

  "run" should {

    "calculate tax deducted amount for UK savings when is no interest from banks" in {
      val liability = aLiability()

      TaxDeductedAmountForUkSavingsIncomeCalculation.run(SelfAssessment(), liability) shouldBe liability.copy(
          incomeTaxDeducted = Some(IncomeTaxDeducted(interestFromUk = 0, total = 0)))
    }

    "calculate tax deducted amount for UK savings income across several unearned incomes considering only taxed interest" in {
      val taxedInterest1 = anUnearnedInterestIncomeSummary("taxedInterest1", InterestFromBanksTaxed, 100)
      val taxedInterest2 = anUnearnedInterestIncomeSummary("taxedInterest2", InterestFromBanksTaxed, 200)
      val untaxedInterest1 = anUnearnedInterestIncomeSummary("untaxedInterest1", InterestFromBanksUntaxed, 500)
      val unearnedIncomes = anUnearnedIncomes().copy(savings = Seq(taxedInterest1, taxedInterest2, untaxedInterest1))
      val liability = aLiability()

      TaxDeductedAmountForUkSavingsIncomeCalculation.run(SelfAssessment(unearnedIncomes = Seq(unearnedIncomes)),
                                                         liability) shouldBe liability.copy(
          incomeTaxDeducted = Some(IncomeTaxDeducted(interestFromUk = 100 + 200, total = 75)))
    }
  }

}
