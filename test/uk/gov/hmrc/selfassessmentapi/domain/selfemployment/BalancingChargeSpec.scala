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

package uk.gov.hmrc.selfassessmentapi.domain.selfemployment

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain.JsonSpec

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
      assertValidationError[BalancingCharge](
        balancingCharge,
        Map("/amount" -> INVALID_MONETARY_AMOUNT),
        "should fail with INVALID_MONETARY_AMOUNT error")
    }

    "reject an negative amount" in {
      val balancingCharge = BalancingCharge(None, BalancingChargeType.BPRA, BigDecimal(-100.12))
      assertValidationError[BalancingCharge](
        balancingCharge,
        Map("/amount" -> INVALID_MONETARY_AMOUNT),
        "should fail with INVALID_MONETARY_AMOUNT error")
    }

    "reject invalid Balancing charge category" in {
      val json = Json.parse(
        """
          |{"type": "BAZ",
          |"amount" : 10000.45
          |}
        """.
          stripMargin)

      assertValidationError[BalancingCharge](
        json,
        Map("/type" -> NO_VALUE_FOUND),
        "should fail with NO_VALUE_FOUND error")
    }

  }
}
