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

import org.joda.time.LocalDate
import uk.gov.hmrc.selfassessmentapi.resources.models.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.{Adjustments, Allowances}

class SelfEmploymentSpec extends JsonSpec {

  "format" should {
    "round trip valid SelfEmployment json" in {
      roundTripJson(SelfEmployment(
        commencementDate = new LocalDate(2016, 4, 22),
        allowances = Some(Allowances(annualInvestmentAllowance = Some(BigDecimal(10))))))
    }
  }

  "validate" should {
    "reject invalid allowances" in {

      val se = SelfEmployment(
        commencementDate = new LocalDate(2016, 4, 22),
        allowances = Some(Allowances(annualInvestmentAllowance = Some(BigDecimal(-10)))))

      assertValidationErrorWithCode(
        se,
        "/allowances/annualInvestmentAllowance", INVALID_MONETARY_AMOUNT)
    }

    "reject invalid adjustments" in {
      val se = SelfEmployment(
        commencementDate = new LocalDate(2016, 4, 22),
        adjustments = Some(Adjustments(lossBroughtForward = Some(BigDecimal(-10)))))

      assertValidationErrorWithCode(
        se,
        "/adjustments/lossBroughtForward", INVALID_MONETARY_AMOUNT)
    }

  }
}
