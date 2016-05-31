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

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain.JsonSpec
import uk.gov.hmrc.selfassessmentapi.domain.employment.IncomeType._

class IncomeSpec extends JsonSpec {

  "format" should {
    "round trip Income json" in {
      IncomeType.values.foreach { IncomeType =>
        roundTripJson(Income(`type` = IncomeType, amount = BigDecimal(1000)))
      }
    }
  }

  "validate" should {

    "reject invalid Income type" in {
      val json = Json.parse(
        """
          |{ "type": "Blah",
          |"amount" : 10000.45
          |}
        """.stripMargin)

      assertValidationError[Income](
        json,
        Map("/type" -> NO_VALUE_FOUND), "Expected invalid employments-income")
    }

    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.12456), BigDecimal(1000.123454), BigDecimal(1000.123456789)).foreach { testAmount =>
        assertValidationError[Income](
          Income(`type` = Salary, amount = testAmount),
          Map("/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid employments-income")
      }
    }

    "reject negative amount" in {
      assertValidationError[Income](
        Income(`type` = Salary, amount = BigDecimal(-1000.13)),
        Map("/amount" -> INVALID_MONETARY_AMOUNT), "Expected negative employments-income")
    }
  }
}
