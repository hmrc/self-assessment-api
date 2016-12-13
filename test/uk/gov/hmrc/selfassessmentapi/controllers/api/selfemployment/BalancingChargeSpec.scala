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

package uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode
import ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec

class BalancingChargeSpec extends JsonSpec {

  "format" should {
    "round trip valid BalancingCharge json" in {
      roundTripJson(BalancingCharge(None, BalancingChargeType.Other, BigDecimal(100.12)))
      roundTripJson(BalancingCharge(None, BalancingChargeType.BPRA, BigDecimal(100.12)))
    }
  }

  "validate" should {
    "reject an amount which is more than 2 decimal places" in {
      val balancingCharge = BalancingCharge(None, BalancingChargeType.Other, BigDecimal(100.123))
      assertValidationErrorWithCode(
        balancingCharge,
        "/amount", INVALID_MONETARY_AMOUNT)
    }

    "reject an negative amount" in {
      val balancingCharge = BalancingCharge(None, BalancingChargeType.BPRA, BigDecimal(-100.12))
      assertValidationErrorWithCode(
        balancingCharge,
        "/amount", INVALID_MONETARY_AMOUNT)
    }

    "reject invalid Balancing charge category" in {
      val json = Json.parse(
        """
          |{"type": "BAZ",
          |"amount" : 10000.45
          |}
        """.
          stripMargin)

      assertValidationErrorsWithCode[BalancingCharge](
        json,
        Map("/type" -> INVALID_VALUE))
    }

  }
}
