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
import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoLiability

object TaxDeductedAmountForUkSavingsIncomeCalculation extends CalculationStep {
  override def run(selfAssessment: SelfAssessment, liability: MongoLiability): MongoLiability = {
    val totalInterest = selfAssessment.unearnedIncomes.map { unearnedIncome =>
      unearnedIncome.savings.filter(_.`type` == InterestFromBanksTaxed).map(_.amount).sum
    }.sum
    val grossingUpPercentage = BigDecimal("0.25")
    val totalTaxDeducted = roundUp(grossingUpPercentage * totalInterest)
    liability.copy(
        incomeTaxDeducted = Some(IncomeTaxDeducted(interestFromUk = totalInterest, total = totalTaxDeducted)))
  }
}
