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

package uk.gov.hmrc.selfassessmentapi.resources.models

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class AccountingPeriodSpec extends JsonSpec {
  "AccountingPeriod" should {
    "round trip" in {
      val accPeriod = AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2017-04-02"))
      roundTripJson(accPeriod)
    }
  }

  "validate" should {
    "reject an accounting period if the `start` date is before 2017-04-01" in {
      val accPeriod = AccountingPeriod(LocalDate.parse("2017-03-31"), LocalDate.parse("2017-04-02"))
      assertValidationErrorWithCode(accPeriod, "/start", ErrorCode.DATE_NOT_IN_THE_PAST)
    }

    "reject an accounting period if the `end` date comes before the `start` date" in {
      val accPeriod = AccountingPeriod(LocalDate.parse("2017-04-02"), LocalDate.parse("2017-04-01"))
      assertValidationErrorWithCode(accPeriod, "", ErrorCode.INVALID_ACCOUNTING_PERIOD)
    }

    "reject an accounting period if any fields are missing" in {
      assertValidationErrorsWithMessage[AccountingPeriod](Json.obj(),
        Map(
          "/start" -> "error.path.missing",
          "/end" -> "error.path.missing"))
    }

    "accept an accounting period if the `start` date comes before the `end` date and the `start` date is after 2017-04-01" in {
      val accPeriod = AccountingPeriod(LocalDate.parse("2017-04-01"), LocalDate.parse("2018-03-31"))
      assertValidationPasses(accPeriod)
    }
  }
}
