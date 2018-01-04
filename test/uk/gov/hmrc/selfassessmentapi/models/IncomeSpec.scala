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

package uk.gov.hmrc.selfassessmentapi.models

import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class IncomeSpec extends JsonSpec {
  "Income" should {
    "round trip" in {
      roundTripJson(Income(500.55, Some(100.11)))
    }

    "reject payloads where the taxDeducted is greater than the amount" in {
      assertValidationErrorWithCode(Income(500.55, Some(500.56)), "", ErrorCode.INVALID_TAX_DEDUCTION_AMOUNT)
    }

    "reject payload where the amount is negative" in {
      assertValidationErrorWithCode(Income(-200.55, None), "/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject payloads where the taxDeducted is negative" in {
      assertValidationErrorWithCode(Income(500.55, Some(-100.11)), "/taxDeducted", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject payloads where the amount is more than 99999999999999.98" in {
      assertValidationErrorWithCode(Income(BigDecimal("99999999999999.99"), None), "/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject payloads where the taxDeducted is more than 99999999999999.98" in {
      assertValidationErrorWithCode(Income(500.55, Some(BigDecimal("99999999999999.99"))), "/taxDeducted", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "accept payloads where the taxDeducted is equal to the amount" in {
      assertValidationPasses(Income(500.55, Some(500.55)))
    }

    "accept payloads where the taxDeducted is less than the amount" in {
      assertValidationPasses(Income(500.55, Some(500.54)))
    }
  }

}
