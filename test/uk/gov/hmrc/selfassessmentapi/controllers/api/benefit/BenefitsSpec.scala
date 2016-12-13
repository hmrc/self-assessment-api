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

package uk.gov.hmrc.selfassessmentapi.controllers.api.benefit

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode
import ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.benefit.BenefitType._

class BenefitsSpec extends JsonSpec {

  "format" should {

    "round trip valid Benefit json" in {
      roundTripJson(Income(`type` = JobSeekersAllowance, amount = BigDecimal(2000.49), taxDeduction = BigDecimal(300.00)))
    }
  }

  "validate" should {
    "reject invalid Benefit type" in {
      val json = Json.parse(
        """
          |{ "type" : "FOO",
          |"amount" : 10000.45,
          |"taxDeduction" : 4000.00
          |}
        """.stripMargin)

      assertValidationErrorsWithCode[Income](
        json, Map("/type" -> INVALID_VALUE))
    }

    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach { testAmount =>
        assertValidationErrorWithCode(
          Income(`type` = JobSeekersAllowance, amount = testAmount, taxDeduction = BigDecimal(304.00)),
          "/amount", INVALID_MONETARY_AMOUNT)
      }
    }

    "reject negative amount" in {
      val benefits = Income(`type` = JobSeekersAllowance, amount = BigDecimal(-1000.12), taxDeduction = BigDecimal(300.09))
      assertValidationErrorWithCode(
        benefits, "/amount", INVALID_MONETARY_AMOUNT)
    }

    "reject tax deductions greater than the amount" in {
      val benefits = Income(`type` = JobSeekersAllowance, amount = BigDecimal(5000.00), taxDeduction = BigDecimal(6000.00))
      assertValidationErrorWithCode(
        benefits, "", INVALID_TAX_DEDUCTION_AMOUNT)
    }
  }
}
