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
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode.{apply => _, _}
import uk.gov.hmrc.selfassessmentapi.domain.JsonSpec
import uk.gov.hmrc.selfassessmentapi.domain.employment.BenefitType._

class BenefitSpec extends JsonSpec {

  "format" should {
    "round trip Benefit json" in {
      BenefitType.values.foreach {
        cat => roundTripJson(Benefit(`type` = cat, amount = BigDecimal(1000.99)))
      }
    }
  }

  "validate" should {
    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach { testAmount =>
        val empBenefit = Benefit(`type` = PrivateInsurance, amount = testAmount)
        assertValidationError[Benefit](
          empBenefit,
          Map("/amount" -> INVALID_MONETARY_AMOUNT),
          "Expected invalid employment benefit amount")
      }
    }

    "reject negative monetary amounts" in {
      Seq(BigDecimal(-1000.12), BigDecimal(-10.12)).foreach { testAmount =>
        val empBenefit = Benefit(`type` = PrivateInsurance, amount = testAmount)
        assertValidationError[Benefit](
          empBenefit,
          Map("/amount" -> INVALID_MONETARY_AMOUNT),
          "Expected invalid employment benefit amount")
      }
    }

    "reject negative amount" in {
      val empBenefit = Benefit(`type` = PrivateInsurance, amount = BigDecimal(-1000.12))
      assertValidationError[Benefit](
        empBenefit,
        Map("/amount" -> INVALID_MONETARY_AMOUNT),
        "Expected negative employment benefit amount")
    }

    "reject invalid Benefit category" in {
      val json = Json.parse(
        """
          |{ "type": "BAR",
          |"amount" : 10000.45
          |}
        """.
          stripMargin)

      assertValidationError[Benefit](
        json,
        Map("/type" -> NO_VALUE_FOUND),
        s"Expected benefit type not in {${BenefitType.values.mkString(", ")}}")
    }
  }
}
