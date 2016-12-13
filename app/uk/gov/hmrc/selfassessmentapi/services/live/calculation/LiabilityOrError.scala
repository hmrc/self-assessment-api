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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation

import uk.gov.hmrc.selfassessmentapi.controllers.api.{UkTaxPaidForEmployment, ErrorCode}
import ErrorCode.INVALID_EMPLOYMENT_TAX_PAID
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{Liability, LiabilityError, LiabilityErrors, LiabilityResult}

object LiabilityOrError {
  def apply(liability: Liability): LiabilityResult = {
    liability.taxDeducted.ukTaxesPaidForEmployments match {
      case taxesPaidForEmployments if allTaxPaidAreNegative(taxesPaidForEmployments) =>
        LiabilityErrors.create(
          liability.nino,
          liability.taxYear,
          errors = Seq(
            LiabilityError(INVALID_EMPLOYMENT_TAX_PAID,
                           "The UK tax paid must be positive for at least one employment source")))
      case _ => liability
    }
  }

  private def allTaxPaidAreNegative(taxesPaidForEmployments: Seq[UkTaxPaidForEmployment]) =
    taxesPaidForEmployments.nonEmpty && taxesPaidForEmployments.forall(_.taxPaid < 0)

}
