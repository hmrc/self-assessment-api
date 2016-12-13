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

package uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode
import ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.IncomeType._

class IncomeSpec extends JsonSpec {

  "format" should {

    "round trip valid Income json" in {
      roundTripJson(Income(`type` = RentIncome, amount = BigDecimal(1000.99)))
    }
  }

  "validate" should {
    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach { testAmount =>
        assertValidationErrorWithCode(
          Income(`type` = RentIncome, amount = testAmount),
          "/amount", INVALID_MONETARY_AMOUNT)
      }
    }

    "reject invalid Income type" in {
      val json = Json.parse(
        """
          |{
          |  "type": "FOO",
          |  "amount" : 10000.45
          |}
        """.stripMargin)

      assertValidationErrorsWithCode[Income](
        json,
        Map("/type" -> INVALID_VALUE))
    }

    "reject negative amount" in {
      assertValidationErrorWithCode(
        Income(`type` = RentIncome, amount = BigDecimal(-1000.12)),
        "/amount", INVALID_MONETARY_AMOUNT)
    }
  }
}
