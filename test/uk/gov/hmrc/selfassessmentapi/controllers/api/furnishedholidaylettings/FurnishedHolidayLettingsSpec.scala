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

package uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings

import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode
import ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec

class FurnishedHolidayLettingsSpec extends JsonSpec {

  "FurnishedHolidayLettings" should {

    "make a valid json round trip" in {
      roundTripJson(FurnishedHolidayLetting(None, PropertyLocationType.UK, None, None))

      roundTripJson(FurnishedHolidayLetting(None, PropertyLocationType.UK,
        Some(Allowances(Some(BigDecimal(1000.00)))),
        Some(Adjustments(Some(BigDecimal(500.00))))))
    }

    "reject capitalAllowance with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val fhl = FurnishedHolidayLetting(None, PropertyLocationType.UK,
          Some(Allowances(Some(amount))),
          Some(Adjustments(Some(BigDecimal(500.00)))))
        assertValidationErrorWithCode(
          fhl,
          "/allowances/capitalAllowance", INVALID_MONETARY_AMOUNT)
      }
    }

    "reject lossBroughtForward with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val fhl = FurnishedHolidayLetting(None, PropertyLocationType.UK,
          Some(Allowances(Some(BigDecimal(500.00)))),
          Some(Adjustments(Some(amount))))
        assertValidationErrorWithCode(
          fhl,
          "/adjustments/lossBroughtForward", INVALID_MONETARY_AMOUNT)
      }
    }

  }

}
