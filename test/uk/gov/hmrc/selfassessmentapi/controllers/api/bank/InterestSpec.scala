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

package uk.gov.hmrc.selfassessmentapi.controllers.api.bank

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.bank.InterestType._

class InterestSpec extends JsonSpec {

  "format" should {

    "round trip valid Income json" in {
      roundTripJson(Interest(`type` = Taxed, amount = BigDecimal(1000.99)))
    }
  }

  "validate" should {
    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach { testAmount =>
        assertValidationError[Interest](
          Interest(`type` = Taxed, amount = testAmount),
          Map("/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid income with more than 2 decimal places")
      }
    }

    "reject invalid Income type" in {
      val json = Json.parse(
        """
          |{ "type": "FOO",
          |"amount" : 10000.45
          |}
        """.stripMargin)

      assertValidationError[Interest](
        json, Map("/type" -> NO_VALUE_FOUND), "should fail with invalid type")
    }

    "reject negative amount" in {
      val seIncome = Interest(`type` = Taxed, amount = BigDecimal(-1000.12))
      assertValidationError[Interest](
        seIncome, Map("/amount" -> INVALID_MONETARY_AMOUNT), "should fail with INVALID_MONETARY_AMOUNT error")
    }
  }
}
