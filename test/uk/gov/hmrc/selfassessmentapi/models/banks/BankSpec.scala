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

package uk.gov.hmrc.selfassessmentapi.models.banks

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode

class BankSpec extends JsonSpec {

  "Bank JSON" should {

    "round trip" in {
      roundTripJson(Bank(None, Some("name")))
    }

    "round trip empty json" in {
      roundTripJson(Bank(None, None))
    }

    "should ignore the id if it is provided by the user" in {
      val input = Bank(Some("myid"), None)
      val expectedOutput = input.copy(id = None)
      assertJsonIs(input, expectedOutput)
    }

    "return a INVALID_FIELD_LENGTH error when account name is too long" in {
      val input = Bank(None, Some("A way toooooooooooooo long bank account name"))
      assertValidationErrorWithCode(input, "/accountName", ErrorCode.INVALID_FIELD_LENGTH)
    }

    "return a INVALID_VALUE error when the account name is non-alphanumeric" in {
      val input = Bank(None, Some("Oh No!"))
      assertValidationErrorWithCode(input, "/accountName", ErrorCode.INVALID_VALUE)
    }

    "return a INVALID_VALUE and INVALID_FIELD_LENGTH error when the account name string is empty" in {
      val input = Bank(None, Some(""))
      assertValidationErrorsWithCode[Bank](Json.toJson(input),
        Map("/accountName" -> Seq(ErrorCode.INVALID_VALUE, ErrorCode.INVALID_FIELD_LENGTH)))
    }
  }
}
