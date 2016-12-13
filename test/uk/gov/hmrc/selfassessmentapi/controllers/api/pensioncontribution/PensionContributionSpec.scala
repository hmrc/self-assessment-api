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

package uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution

import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode
import ErrorCode._
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec

class PensionContributionSpec extends JsonSpec {

  "format" should {
    "round trip valid PensionContribution json" in {
      roundTripJson(PensionContribution.example())
    }
  }

  "validate" should {
    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach { testAmount =>
        assertValidationErrorWithCode(
          PensionContribution(ukRegisteredPension = Some(testAmount)),
          "/ukRegisteredPension", INVALID_MONETARY_AMOUNT)
        assertValidationErrorWithCode(
          PensionContribution(retirementAnnuity = Some(testAmount)),
          "/retirementAnnuity", INVALID_MONETARY_AMOUNT)
        assertValidationErrorWithCode(
          PensionContribution(employerScheme = Some(testAmount)),
          "/employerScheme", INVALID_MONETARY_AMOUNT)
        assertValidationErrorWithCode(
          PensionContribution(overseasPension = Some(testAmount)),
          "/overseasPension", INVALID_MONETARY_AMOUNT)
        assertValidationErrorWithCode(PensionSaving(Some(testAmount), None),
          "/excessOfAnnualAllowance", INVALID_MONETARY_AMOUNT)
        assertValidationErrorWithCode(PensionSaving(None, Some(testAmount)),
          "/taxPaidByPensionScheme", INVALID_MONETARY_AMOUNT)
      }
    }

    "reject negative amount" in {
      Seq(BigDecimal(-1000.12), BigDecimal(-1000)).foreach { testAmount =>
        assertValidationErrorWithCode(
          PensionContribution(ukRegisteredPension = Some(testAmount)),
          "/ukRegisteredPension", INVALID_MONETARY_AMOUNT)
        assertValidationErrorWithCode(
          PensionContribution(retirementAnnuity = Some(testAmount)),
          "/retirementAnnuity", INVALID_MONETARY_AMOUNT)
        assertValidationErrorWithCode(
          PensionContribution(employerScheme = Some(testAmount)),
          "/employerScheme", INVALID_MONETARY_AMOUNT)
        assertValidationErrorWithCode(
          PensionContribution(overseasPension = Some(testAmount)),
          "/overseasPension", INVALID_MONETARY_AMOUNT)
        assertValidationErrorWithCode(
          PensionSaving(Some(testAmount), None),
          "/excessOfAnnualAllowance", INVALID_MONETARY_AMOUNT)
        assertValidationErrorWithCode(
          PensionSaving(None, Some(testAmount)),
          "/taxPaidByPensionScheme", INVALID_MONETARY_AMOUNT)
      }
    }

    "reject pensionSavings when there are no pension contributions" in {
      assertValidationErrorWithCode(
        PensionContribution(pensionSavings = Some(PensionSaving(excessOfAnnualAllowance = None, taxPaidByPensionScheme = None))),
        "", UNDEFINED_REQUIRED_ELEMENT)
    }

    "reject pensionSavings when the sum of pensionSavings exceeds the sum of all other pension contributions" in {
      assertValidationErrorWithCode(
        PensionContribution(employerScheme = Some(100), pensionSavings = Some(PensionSaving(excessOfAnnualAllowance = Some(500), taxPaidByPensionScheme = Some(500)))),
        "", MAXIMUM_AMOUNT_EXCEEDED)
    }
  }

  "PensionSavings" should {
    "reject taxPaidByPensionScheme when excessOfAnnualAllowance is undefined" in {
      assertValidationErrorWithCode(
        PensionSaving(excessOfAnnualAllowance = None, taxPaidByPensionScheme = Some(500)),
        "", UNDEFINED_REQUIRED_ELEMENT)
    }

    "reject taxPaidByPensionScheme when its value exceeds the value of the excessOfAnnualAllowance" in {
      assertValidationErrorWithCode(
        PensionSaving(excessOfAnnualAllowance = Some(200), taxPaidByPensionScheme = Some(500)),
        "", MAXIMUM_AMOUNT_EXCEEDED)
    }
  }
}
