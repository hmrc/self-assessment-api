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

package uk.gov.hmrc.selfassessmentapi.models.selfemployment

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.models.{ErrorCode, Expense, SimpleIncome}
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class SelfEmploymentPeriodSpec extends JsonSpec {
  "SelfEmploymentPeriod" should {
    "round trip" in {
      val period = SelfEmploymentPeriod(None, LocalDate.now, LocalDate.now.plusDays(1), None, None)
      roundTripJson(period)
    }

    "return a INVALID_PERIOD error when using a period with a 'from' date that becomes before the 'to' date" in {
      val period = SelfEmploymentPeriod(None, LocalDate.now, LocalDate.now.minusDays(1), None, None)
      assertValidationErrorWithCode(period, "", ErrorCode.INVALID_PERIOD)
    }

    "return a INVALID_MONETARY_AMOUNT error when income contains a negative value" in {

      val period = SelfEmploymentPeriod(None, LocalDate.now.minusDays(1), LocalDate.now, Some(Incomes(Some(SimpleIncome(-5000)), None)), None)

      assertValidationErrorWithCode(period, "/incomes/turnover/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return a INVALID_MONETARY_AMOUNT error when income amount contains more than 2 decimal places" in {
      val period = SelfEmploymentPeriod(None, LocalDate.now.minusDays(1), LocalDate.now, Some(Incomes(Some(SimpleIncome(10.123)), None)), None)

      assertValidationErrorWithCode(period, "/incomes/turnover/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return a INVALID_MONETARY_AMOUNT error when expense contains a negative value" in {

      val period = SelfEmploymentPeriod(
        None, LocalDate.now.minusDays(1), LocalDate.now, None, Some(Expenses(
          costOfGoodsBought = Some(Expense(-500, None)),
          badDebt = Some(Expense(200, Some(100))))))

      assertValidationErrorWithCode(period, "/expenses/costOfGoodsBought/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return a INVALID_MONETARY_AMOUNT error when expense contains more than 2 decimal places" in {
      val period = SelfEmploymentPeriod(
        None, LocalDate.now.minusDays(1), LocalDate.now, None, Some(Expenses(
          costOfGoodsBought = Some(Expense(500.123, None)),
          badDebt = Some(Expense(200, None)))))

      assertValidationErrorWithCode(period, "/expenses/costOfGoodsBought/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return a INVALID_DISALLOWABLE_AMOUNT error when expense disallowableAmount > amount" in {
      val period = SelfEmploymentPeriod(
        None, LocalDate.now.minusDays(1), LocalDate.now, None, Some(Expenses(
          costOfGoodsBought = Some(Expense(500, Some(600))),
          badDebt = Some(Expense(200, Some(100))))))

      assertValidationErrorWithCode(period, "/expenses/costOfGoodsBought", ErrorCode.INVALID_DISALLOWABLE_AMOUNT)
    }

    "return a DEPRECIATION_DISALLOWABLE_AMOUNT error when expense 'amount' and 'disallowableAmount' fields are not equal for depreciations" in {
      val period = SelfEmploymentPeriod(
        None, LocalDate.now.minusDays(1), LocalDate.now, None, Some(Expenses(
          depreciation = Some(Expense(200, Some(100))),
          badDebt = Some(Expense(200, Some(100))))))

      assertValidationErrorWithCode(period, "/expenses", ErrorCode.DEPRECIATION_DISALLOWABLE_AMOUNT)
    }

    "return an error when provided with an empty json body" in {
      assertValidationErrorsWithMessage[SelfEmploymentPeriod](Json.parse("{}"),
        Map("/from" -> Seq("error.path.missing"), "/to" -> Seq("error.path.missing")))
    }

    "pass if the from date is equal to the end date" in {
      val period = SelfEmploymentPeriod(
        id = None,
        from = LocalDate.parse("2017-04-01"),
        to = LocalDate.parse("2017-04-01"),
        incomes = None,
        expenses = None)

      assertValidationPasses(period)
    }
  }
}
