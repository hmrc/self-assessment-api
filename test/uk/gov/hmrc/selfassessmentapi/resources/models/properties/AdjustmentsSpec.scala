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

package uk.gov.hmrc.selfassessmentapi.resources.models.properties

import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.resources.models.ErrorCode

class AdjustmentsSpec extends JsonSpec {
  "json" should {
    "round trip" in {
      roundTripJson(Adjustments(Some(50)))
    }

    "round trip with empty object" in {
      roundTripJson(Adjustments())
    }
  }

  "adjustments" should {
    "reject lossBroughtForward with a negative value" in {
      assertValidationErrorWithCode(Adjustments(lossBroughtForward = Some(-50)),
        "/lossBroughtForward", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "reject lossBroughtForward with more than 2 decimal places" in {
      assertValidationErrorWithCode(Adjustments(lossBroughtForward = Some(50.123)),
        "/lossBroughtForward", ErrorCode.INVALID_MONETARY_AMOUNT)
    }
  }
}
