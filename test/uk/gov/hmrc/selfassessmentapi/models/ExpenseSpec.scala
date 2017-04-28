/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models

import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class ExpenseSpec extends JsonSpec {
  "Expense" should {
    "round trip" in roundTripJson(Expense(500.55, Some(300)))

    "reject a negative amount" in
      assertValidationErrorWithCode(Expense(-20.20, None), "/amount", ErrorCode.INVALID_MONETARY_AMOUNT)

    "reject an amount with more than 2 decimal places" in
      assertValidationErrorWithCode(Expense(10.123, None), "/amount", ErrorCode.INVALID_MONETARY_AMOUNT)

    "reject an expense where the disallowable amount is greater than the amount" in
      assertValidationErrorWithCode(Expense(30.00, Some(50.00)), "", ErrorCode.INVALID_DISALLOWABLE_AMOUNT)

  }
}
