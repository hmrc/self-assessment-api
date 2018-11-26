/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.r1.selfassessmentapi.models

import play.api.libs.json.Json
import uk.gov.hmrc.r2.selfassessmentapi.resources.JsonSpec

class ExpenseSpec extends JsonSpec {
  "Expense" should {
    "round trip" in roundTripJson(Expense(500.55, Some(300)))

    "reject a negative amount" in
      assertValidationErrorWithCode(Expense(-20.20, None), "/amount", ErrorCode.INVALID_MONETARY_AMOUNT)

    "reject amounts more than 99999999999999.98" in
      assertValidationErrorWithCode(Expense(BigDecimal("99999999999999.99"), None), "/amount", ErrorCode.INVALID_MONETARY_AMOUNT)

    "reject disallowable amounts more than 99999999999999.98" in
      assertValidationErrorWithCode(Expense(BigDecimal("99999999999999.99"), None), "/amount", ErrorCode.INVALID_MONETARY_AMOUNT)

    "reject an amount with more than 2 decimal places" in
      assertValidationErrorWithCode(Expense(10.123, None), "/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
  }

  "ExpenseNegativeOrPositive" should {
    "round trip" in roundTripJson(ExpenseNegativeOrPositive(500.55, Some(300)))

    "accept a negative amount of -99999999999.99" in
      assertValidationPasses(ExpenseNegativeOrPositive(-99999999999.99, Some(-99999999999.99)))

    "reject a negative amount less than -99999999999.99" in
      assertValidationErrorsWithCode[ExpenseNegativeOrPositive](Json.toJson(ExpenseNegativeOrPositive(-100000000000.00, Some(-100000000000.00))),
        Map("/amount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT), "/disallowableAmount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT)))

    "accept a positive amount of 99999999999.99" in
      assertValidationPasses(ExpenseNegativeOrPositive(99999999999.99, Some(99999999999.99)))

    "reject a positive amount greater than 99999999999.99" in
      assertValidationErrorsWithCode[ExpenseNegativeOrPositive](Json.toJson(ExpenseNegativeOrPositive(100000000000.00, Some(100000000000.00))),
        Map("/amount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT), "/disallowableAmount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT)))

    "reject an amount with more than 2 decimal places" in
      assertValidationErrorsWithCode[ExpenseNegativeOrPositive](Json.toJson(ExpenseNegativeOrPositive(10.123, Some(10.123))),
        Map("/amount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT), "/disallowableAmount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT)))
  }

  "ExpenseProfessionalFees" should {
    "round trip" in roundTripJson(ExpenseProfessionalFees(500.55, Some(300)))

    "accept a negative amount of -99999999999.99" in
      assertValidationPasses(ExpenseNegativeOrPositive(-99999999999.99, None))

    "reject a negative amount less than -99999999999.99 and negative disallowableAmount" in
      assertValidationErrorsWithCode[ExpenseProfessionalFees](Json.toJson(ExpenseProfessionalFees(-100000000000.00, Some(-0.01))),
        Map("/amount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT), "/disallowableAmount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT)))

    "accept a positive amount of 99999999999.99 and disallowableAmount of 99999999999999.98" in
      assertValidationPasses(ExpenseProfessionalFees(99999999999.99, Some(99999999999999.98)))

    "reject a positive amount greater than 99999999999.99 and a disallowableAmount greater than 99999999999999.98" in
      assertValidationErrorsWithCode[ExpenseProfessionalFees](Json.toJson(ExpenseProfessionalFees(100000000000.00, Some(BigDecimal("99999999999999.99")))),
        Map("/amount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT), "/disallowableAmount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT)))

    "reject an amount with more than 2 decimal places" in
      assertValidationErrorsWithCode[ExpenseProfessionalFees](Json.toJson(ExpenseProfessionalFees(10.123, Some(10.123))),
        Map("/amount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT), "/disallowableAmount" -> Seq(ErrorCode.INVALID_MONETARY_AMOUNT)))
  }
}
