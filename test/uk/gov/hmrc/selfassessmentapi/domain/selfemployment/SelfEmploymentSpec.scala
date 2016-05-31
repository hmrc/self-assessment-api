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

import org.joda.time.LocalDate
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain.JsonSpec

class SelfEmploymentSpec extends JsonSpec {

  "format" should {
    "round trip valid SelfEmployment json" in {
      roundTripJson(SelfEmployment(
        name = "self employment 1",
        commencementDate = new LocalDate(2016, 4, 22),
        allowances = Some(Allowances(annualInvestmentAllowance = Some(BigDecimal(10))))))
    }
  }

  "validate" should {
    "reject name longer than 100 characters and commencement date after the present date" in {

      val se = SelfEmployment(name = "a" * 101, commencementDate = LocalDate.now().plusDays(1))

      assertValidationError[SelfEmployment](
        se,
        Map("/commencementDate" -> COMMENCEMENT_DATE_NOT_IN_THE_PAST, "/name" -> MAX_FIELD_LENGTH_EXCEEDED),
        "Expected valid self-employment")
    }

    "reject invalid allowances" in {

      val se = SelfEmployment(
        name = "self employment 1",
        commencementDate = new LocalDate(2016, 4, 22),
        allowances = Some(Allowances(annualInvestmentAllowance = Some(BigDecimal(-10)))))

      assertValidationError[SelfEmployment](
        se,
        Map("/allowances/annualInvestmentAllowance" -> INVALID_MONETARY_AMOUNT), "Expected valid self-employment")
    }

    "reject invalid adjustments" in {

      val se = SelfEmployment(
        name = "self employment 1",
        commencementDate = new LocalDate(2016, 4, 22),
        adjustments = Some(Adjustments(lossBroughtForward = Some(BigDecimal(-10)))))

      assertValidationError[SelfEmployment](
        se,
        Map("/adjustments/lossBroughtForward" -> INVALID_MONETARY_AMOUNT), "Expected valid self-employment")
    }

  }
}
