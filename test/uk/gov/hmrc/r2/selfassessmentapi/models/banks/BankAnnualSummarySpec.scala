/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.r2.selfassessmentapi.models.banks

import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode

class BankAnnualSummarySpec extends JsonSpec {
  "BankAnnualSummary" should {
    "round trip" in {
      roundTripJson(BankAnnualSummary(Some(50.55), Some(12.22)))
    }

    "round trip empty json" in {
      roundTripJson(BankAnnualSummary(None, None))
    }

    "reject taxedUkInterest with a negative value" in {
      assertValidationErrorWithCode(BankAnnualSummary(Some(-50.55), Some(22.22)),
        "/taxedUkInterest", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject taxedUkInterest more than 99999999999999.98" in {
      assertValidationErrorWithCode(BankAnnualSummary(Some(BigDecimal("99999999999999.99")), Some(22.22)),
        "/taxedUkInterest", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject taxedUkInterest with more than 2 decimal places" in {
      assertValidationErrorWithCode(BankAnnualSummary(Some(50.555), Some(22.22)),
        "/taxedUkInterest", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject untaxedUkInterest with a negative value" in {
      assertValidationErrorWithCode(BankAnnualSummary(Some(50.55), Some(-22.22)),
        "/untaxedUkInterest", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject untaxedUkInterest more than 99999999999999.98" in {
      assertValidationErrorWithCode(BankAnnualSummary(Some(50.55), Some(BigDecimal("99999999999999.99"))),
        "/untaxedUkInterest", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject untaxedUkInterest with more than 2 decimal places" in {
      assertValidationErrorWithCode(BankAnnualSummary(Some(50.55), Some(22.223)),
        "/untaxedUkInterest", ErrorCode.INVALID_MONETARY_AMOUNT)
    }
  }
}
