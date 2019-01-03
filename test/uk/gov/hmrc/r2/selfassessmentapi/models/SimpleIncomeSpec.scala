/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.r2.selfassessmentapi.models

import uk.gov.hmrc.r2.selfassessmentapi.resources.JsonSpec

class SimpleIncomeSpec extends JsonSpec {
  "SimpleIncome" should {
    "round trip" in {
      roundTripJson(SimpleIncome(500.55))
    }

    "reject a negative amount" in {
      assertValidationErrorWithCode(SimpleIncome(-20.20), "/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject amounts more than 99999999999999.98" in {
      assertValidationErrorWithCode(SimpleIncome(BigDecimal("99999999999999.99")), "/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject an amount with more than 2 decimal places" in {
      assertValidationErrorWithCode(SimpleIncome(10.123), "/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }
  }
}
