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

package uk.gov.hmrc.selfassessmentapi.models.properties

import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode

class FHLPropertiesAdjustmentsSpec extends JsonSpec {
  "FHLPropertiesAdjustments" should {
    "round trip" in {
      roundTripJson(FHLPropertiesAdjustments(Some(50.55), Some(12.22), Some(123.45)))
    }

    "round trip with empty json" in {
      roundTripJson(FHLPropertiesAdjustments())
    }
  }

  "validate" should {
    "reject lossBroughtForward with a negative value" in {
      assertValidationErrorWithCode(FHLPropertiesAdjustments(lossBroughtForward = Some(-50)),
        "/lossBroughtForward", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject zeroEmissionGoodsVehicleAllowance more than 99999999999999.98" in {
      assertValidationErrorWithCode(FHLPropertiesAdjustments(lossBroughtForward = Some(BigDecimal("99999999999999.99"))),
        "/lossBroughtForward", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject lossBroughtForward with more than 2 decimal places" in {
      assertValidationErrorWithCode(FHLPropertiesAdjustments(lossBroughtForward = Some(50.123)),
        "/lossBroughtForward", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject privateUseAdjustment with a negative value" in {
      assertValidationErrorWithCode(FHLPropertiesAdjustments(privateUseAdjustment = Some(-50)),
        "/privateUseAdjustment", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject privateUseAdjustment more than 99999999999999.98" in {
      assertValidationErrorWithCode(FHLPropertiesAdjustments(privateUseAdjustment = Some(BigDecimal("99999999999999.99"))),
        "/privateUseAdjustment", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject privateUseAdjustment with more than 2 decimal places" in {
      assertValidationErrorWithCode(FHLPropertiesAdjustments(privateUseAdjustment = Some(50.123)),
        "/privateUseAdjustment", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject balancingCharge with a negative value" in {
      assertValidationErrorWithCode(FHLPropertiesAdjustments(balancingCharge = Some(-50)),
        "/balancingCharge", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject balancingCharge more than 99999999999999.98" in {
      assertValidationErrorWithCode(FHLPropertiesAdjustments(balancingCharge = Some(BigDecimal("99999999999999.99"))),
        "/balancingCharge", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject balancingCharge with more than 2 decimal places" in {
      assertValidationErrorWithCode(FHLPropertiesAdjustments(balancingCharge = Some(50.123)),
        "/balancingCharge", ErrorCode.INVALID_MONETARY_AMOUNT)
    }
  }
}
