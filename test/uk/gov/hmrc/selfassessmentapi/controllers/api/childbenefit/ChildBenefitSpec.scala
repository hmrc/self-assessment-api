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

package uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit

import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode
import ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec

class ChildBenefitSpec extends JsonSpec {

  "format" should {
    "round trip ChildBenefit json" in {
        roundTripJson(ChildBenefit(amount = BigDecimal(1000), numberOfChildren = 3))
    }
  }

  "validate" should {

    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.12456), BigDecimal(1000.123454), BigDecimal(1000.123456789)).foreach { testAmount =>
        val expense = ChildBenefit(amount = testAmount, numberOfChildren = 3)
        assertValidationErrorWithCode(
          expense,
          "/amount", INVALID_MONETARY_AMOUNT)
      }
    }

    "reject negative amount" in {
      val expense = ChildBenefit(amount = BigDecimal(-1000.13), numberOfChildren = 3)
      assertValidationErrorWithCode(
        expense,
        "/amount", INVALID_MONETARY_AMOUNT)
    }

    "reject zero and negative number of children" in {
      Seq(-2, -5).foreach {
        testNumber =>
          val expense = ChildBenefit(amount = BigDecimal(1000), numberOfChildren = testNumber)
          assertValidationErrorWithCode(
            expense,
            "/numberOfChildren", VALUE_BELOW_MINIMUM)
      }
    }

    "reject non zero amount and zero number of children" in {
      Seq(BigDecimal(123.34), BigDecimal(1000.23)).foreach {
        testAmount =>
          val expense = ChildBenefit(amount = testAmount, numberOfChildren = 0)
          assertValidationErrorWithCode(
            expense, "", VALUE_BELOW_MINIMUM)
      }
    }

    "allow zero amount and non zero number of children" in {
      Seq(1,2,3,4).foreach {
        testNumberOfChildren =>
          assertValidationPasses[ChildBenefit](ChildBenefit(amount = 0, numberOfChildren = testNumberOfChildren))
      }
    }
  }
}
