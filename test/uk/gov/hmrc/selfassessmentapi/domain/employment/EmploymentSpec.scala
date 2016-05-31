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

package uk.gov.hmrc.selfassessmentapi.domain.employment

import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain.JsonSpec

class EmploymentSpec extends JsonSpec {

  "format" should {
    "round trip valid SelfEmployment json" in {
      roundTripJson(Employment(name = "employment 1"))
    }
  }

  "validate" should {
    "reject name longer than 100 characters" in {

      val se = Employment(name = "a" * 101)

      assertValidationError[Employment](
        se,
        Map("/name" -> MAX_FIELD_LENGTH_EXCEEDED), "Expected valid employment")
    }
  }
}
