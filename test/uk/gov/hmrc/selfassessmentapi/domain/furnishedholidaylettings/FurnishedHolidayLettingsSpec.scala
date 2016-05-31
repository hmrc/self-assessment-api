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

package uk.gov.hmrc.selfassessmentapi.domain.furnishedholidaylettings

import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain.JsonSpec

class FurnishedHolidayLettingsSpec extends JsonSpec {

  "FurnishedHolidayLettings" should {

    "make a valid json round trip" in {
      roundTripJson(FurnishedHolidayLetting(None, "Cosa del Sol apartment", PropertyLocationType.UK, None, None))

      roundTripJson(FurnishedHolidayLetting(None, "Cosa del Sol apartment", PropertyLocationType.UK,
        Some(Allowances(Some(BigDecimal(1000.00)))),
        Some(Adjustments(Some(BigDecimal(500.00))))))
    }

    "reject name with more than 100 characters" in {
      val fhl = FurnishedHolidayLetting(None, "Abcd" * 100, PropertyLocationType.UK,
        Some(Allowances(Some(BigDecimal(1000.00)))),
        Some(Adjustments(Some(BigDecimal(500.00)))))
        assertValidationError[FurnishedHolidayLetting](
        fhl,
        Map("/name" -> MAX_FIELD_LENGTH_EXCEEDED),
        "Expected invalid furnished-holiday-lettings")
    }


    "reject capitalAllowance with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val fhl = FurnishedHolidayLetting(None, "Cosa del Sol apartment", PropertyLocationType.UK,
          Some(Allowances(Some(amount))),
          Some(Adjustments(Some(BigDecimal(500.00)))))
          assertValidationError[FurnishedHolidayLetting](
          fhl,
          Map("/allowances/capitalAllowance" -> INVALID_MONETARY_AMOUNT), "Expected invalid furnished-holiday-lettings")
      }
    }

    "reject lossBroughtForward with negative amounts" in {
      Seq(BigDecimal(-1213.00), BigDecimal(-2243434.00)).foreach { amount =>
        val fhl = FurnishedHolidayLetting(None, "Cosa del Sol apartment", PropertyLocationType.UK,
          Some(Allowances(Some(BigDecimal(500.00)))),
          Some(Adjustments(Some(amount))))
          assertValidationError[FurnishedHolidayLetting](
          fhl,
          Map("/adjustments/lossBroughtForward" -> INVALID_MONETARY_AMOUNT), "Expected invalid furnished-holiday-lettings")
      }
    }

  }

}
