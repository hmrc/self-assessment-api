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

package uk.gov.hmrc.r2.selfassessmentapi.models.properties

import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode

class OtherPropertiesAdjustmentsSpec extends JsonSpec {
  "json" should {
    "round trip" in {
      roundTripJson(OtherPropertiesAdjustments(Some(50.12), Some(12.34), Some(66.34)))
    }

    "round trip with empty object" in {
      roundTripJson(OtherPropertiesAdjustments())
    }
  }

  "adjustments" should {
    "reject lossBroughtForward with a negative value" in {
      assertValidationErrorWithCode(OtherPropertiesAdjustments(lossBroughtForward = Some(-50)),
        "/lossBroughtForward", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject lossBroughtForward more than 99999999999999.98" in {
      assertValidationErrorWithCode(OtherPropertiesAdjustments(lossBroughtForward = Some(BigDecimal("99999999999999.99"))),
        "/lossBroughtForward", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject lossBroughtForward with more than 2 decimal places" in {
      assertValidationErrorWithCode(OtherPropertiesAdjustments(lossBroughtForward = Some(50.123)),
        "/lossBroughtForward", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject privateUseAdjustment with a negative value" in {
      assertValidationErrorWithCode(OtherPropertiesAdjustments(privateUseAdjustment = Some(-50)),
        "/privateUseAdjustment", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject privateUseAdjustment more than 99999999999999.98" in {
      assertValidationErrorWithCode(OtherPropertiesAdjustments(privateUseAdjustment = Some(BigDecimal("99999999999999.99"))),
        "/privateUseAdjustment", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject privateUseAdjustment with more than 2 decimal places" in {
      assertValidationErrorWithCode(OtherPropertiesAdjustments(privateUseAdjustment = Some(50.123)),
        "/privateUseAdjustment", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject balancingCharge with a negative value" in {
      assertValidationErrorWithCode(OtherPropertiesAdjustments(balancingCharge = Some(-50)),
        "/balancingCharge", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject balancingCharge more than 99999999999999.98" in {
      assertValidationErrorWithCode(OtherPropertiesAdjustments(balancingCharge = Some(BigDecimal("99999999999999.99"))),
        "/balancingCharge", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject balancingCharge with more than 2 decimal places" in {
      assertValidationErrorWithCode(OtherPropertiesAdjustments(balancingCharge = Some(50.123)),
        "/balancingCharge", ErrorCode.INVALID_MONETARY_AMOUNT)
    }
  }
}
