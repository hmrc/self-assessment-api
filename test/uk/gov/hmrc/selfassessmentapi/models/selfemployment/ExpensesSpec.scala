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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import uk.gov.hmrc.selfassessmentapi.models.{ErrorCode, Expense}
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import play.api.libs.json.Json.toJson

class ExpensesSpec extends JsonSpec {

  "Expenses" should {

    "round trip in" in
      roundTripJson(Expenses(depreciation = Some(Expense(200, Some(200))), badDebt = Some(Expense(200, Some(100)))))

    "reject Expenses with depreciation expense where disallowable amount is not equal to amount" in
      assertValidationErrorWithCode(
        Expenses(depreciation = Some(Expense(200, Some(100))), badDebt = Some(Expense(200, Some(100)))),
        "/depreciation/disallowableAmount",
        ErrorCode.DEPRECIATION_DISALLOWABLE_AMOUNT
      )

    "reject Expenses where the disallowable amount is greater than the amount" in
      assertValidationErrorWithCode(Expenses(costOfGoodsBought = Some(Expense(30.00, Some(50.00)))),
                                    "/costOfGoodsBought/disallowableAmount",
                                    ErrorCode.INVALID_DISALLOWABLE_AMOUNT)

    "reject Expenses with multiple validation errors" in
      assertValidationErrorsWithCode[Expenses](
        toJson(
          Expenses(depreciation = Some(Expense(200, Some(100))),
                   badDebt = Some(Expense(200, Some(100))),
                   costOfGoodsBought = Some(Expense(30.00, Some(50.00))))),
        Map(
          "/depreciation/disallowableAmount" -> Seq(ErrorCode.DEPRECIATION_DISALLOWABLE_AMOUNT),
          "/costOfGoodsBought/disallowableAmount" -> Seq(ErrorCode.INVALID_DISALLOWABLE_AMOUNT)
        )
      )

    "accept Expenses with depreciation expense where disallowable amount is not defined" in
      roundTripJson(Expenses(depreciation = Some(Expense(200, None))))
  }

}
