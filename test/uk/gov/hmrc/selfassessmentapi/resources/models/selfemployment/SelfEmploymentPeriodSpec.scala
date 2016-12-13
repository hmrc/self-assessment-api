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

package uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.resources.models.{ErrorCode, Expense, Income}

class SelfEmploymentPeriodSpec extends JsonSpec {
  "SelfEmploymentPeriod" should {
    "round trip" in {
      val period = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(1), SelfEmploymentPeriodicData(Map.empty, Map.empty))
      roundTripJson(period)
    }

    "return a INVALID_PERIOD error when using a period with a 'from' date that becomes before the 'to' date" in {
      val period = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.minusDays(1), SelfEmploymentPeriodicData(Map.empty, Map.empty))
      assertValidationErrorWithCode(period, "", ErrorCode.INVALID_PERIOD)
    }

    "return a INVALID_MONETARY_AMOUNT error when income contains a negative value" in {
      val period = SelfEmploymentPeriod(LocalDate.now.minusDays(1), LocalDate.now, SelfEmploymentPeriodicData(Map(IncomeType.Turnover -> Income(-5000)), Map.empty))

      assertValidationErrorWithCode(period, "/incomes/turnover/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return a INVALID_MONETARY_AMOUNT error when income amount contains more than 2 decimal places" in {
      val period = SelfEmploymentPeriod(LocalDate.now.minusDays(1), LocalDate.now, SelfEmploymentPeriodicData(Map(IncomeType.Turnover -> Income(10.123)), Map.empty))

      assertValidationErrorWithCode(period, "/incomes/turnover/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return a INVALID_MONETARY_AMOUNT error when expense contains a negative value" in {
      val period = SelfEmploymentPeriod(
        LocalDate.now.minusDays(1), LocalDate.now, SelfEmploymentPeriodicData(Map.empty, Map(ExpenseType.CostOfGoodsBought -> Expense(-500, None),
          ExpenseType.BadDebt -> Expense(200, Some(100)))))

      assertValidationErrorWithCode(period, "/expenses/costOfGoodsBought/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return a INVALID_MONETARY_AMOUNT error when expense contains more than 2 decimal places" in {
      val period = SelfEmploymentPeriod(
        LocalDate.now.minusDays(1), LocalDate.now, SelfEmploymentPeriodicData(Map.empty, Map(ExpenseType.CostOfGoodsBought -> Expense(500.123, None),
          ExpenseType.BadDebt -> Expense(200, None))))

      assertValidationErrorWithCode(period, "/expenses/costOfGoodsBought/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return a INVALID_DISALLOWABLE_AMOUNT error when expense disallowableAmount > amount" in {
      val period = SelfEmploymentPeriod(
        LocalDate.now.minusDays(1), LocalDate.now, SelfEmploymentPeriodicData(Map.empty, Map(ExpenseType.CostOfGoodsBought -> Expense(500, Some(600)),
          ExpenseType.BadDebt -> Expense(200, Some(100)))))

      assertValidationErrorWithCode(period, "/expenses/costOfGoodsBought", ErrorCode.INVALID_DISALLOWABLE_AMOUNT)
    }

    "return a DEPRECIATION_DISALLOWABLE_AMOUNT error when expense 'amount' and 'disallowableAmount' fields are not equal for depreciations" in {
      val period = SelfEmploymentPeriod(
        LocalDate.now.minusDays(1), LocalDate.now, SelfEmploymentPeriodicData(Map.empty, Map(ExpenseType.Depreciation -> Expense(200, Some(100)),
          ExpenseType.BadDebt -> Expense(200, Some(100)))))

      assertValidationErrorWithCode(period, "/expenses", ErrorCode.DEPRECIATION_DISALLOWABLE_AMOUNT)
    }

    "return an error when provided with an empty json body" in {
      assertValidationErrorsWithMessage[SelfEmploymentPeriod](Json.parse("{}"),
        Map("/from" -> "error.path.missing", "/to" -> "error.path.missing"))
    }

    "pass if the from date is equal to the end date" in {
      val period = SelfEmploymentPeriod(
        from = LocalDate.parse("2017-04-01"),
        to = LocalDate.parse("2017-04-01"),
        data = SelfEmploymentPeriodicData(Map.empty, Map.empty))

      assertValidationPasses(period)
    }
  }
}
