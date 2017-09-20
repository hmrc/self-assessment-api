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

package uk.gov.hmrc.selfassessmentapi.models

import uk.gov.hmrc.selfassessmentapi.models.Class4NicsExemptionCode.NON_RESIDENT
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class Class4NicInfoSpec extends JsonSpec {

  "Class4NicInfo" should {

    "do successful round trip" in roundTripJson(Class4NicInfo(Some(true), Some(NON_RESIDENT)))

    "pass if not exempt and the exemption code not defined" in roundTripJson(Class4NicInfo(Some(false), None))

    "reject if exempt but tee exemption code is missing" in
      assertValidationErrorWithCode(Class4NicInfo(Some(true), None), "", MANDATORY_FIELD_MISSING)

    "reject if not exempt and the exemption code defined" in
      assertValidationErrorWithCode(Class4NicInfo(Some(false), Some(NON_RESIDENT)), "", INVALID_VALUE)
  }
}
