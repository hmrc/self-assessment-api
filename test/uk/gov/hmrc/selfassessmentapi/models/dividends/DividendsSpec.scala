/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.dividends

import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode

class DividendsSpec extends JsonSpec {
  "Dividends" should {
    "round trip" in {
      roundTripJson(Dividends(Some(200.25)))
    }

    "round trip empty json" in {
      roundTripJson(Dividends(None))
    }
  }

  "validate" should {
    "reject ukDividends with a negative value" in {
      assertValidationErrorWithCode(Dividends(ukDividends = Some(-50)),
        "/ukDividends", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject ukDividends more than 99999999999999.98" in {
      assertValidationErrorWithCode(Dividends(ukDividends = Some(BigDecimal("99999999999999.99"))),
        "/ukDividends", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject annualInvestmentAllowance with more than 2 decimal places" in {
      assertValidationErrorWithCode(Dividends(ukDividends = Some(50.123)),
        "/ukDividends", ErrorCode.INVALID_MONETARY_AMOUNT)
    }
  }
}
