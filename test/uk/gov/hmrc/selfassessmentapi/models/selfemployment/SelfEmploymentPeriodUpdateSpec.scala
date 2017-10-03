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

import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.models.ErrorCode
import uk.gov.hmrc.selfassessmentapi.models.Generators.{genExpenses, genIncomes}
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class SelfEmploymentPeriodUpdateSpec extends JsonSpec with GeneratorDrivenPropertyChecks {

  "SelfEmploymentPeriodUpdate" should {
    "round trip" in forAll(genSelfEmploymentPeriodUpdate())(roundTripJson(_))

    "return a NO_INCOMES_AND_EXPENSES error when incomes and expenses are not supplied" in
      forAll(genSelfEmploymentPeriodUpdate(nullFinancials = true)) { period =>
        assertValidationErrorsWithCode[SelfEmploymentPeriodUpdate](Json.toJson(period),
                                                                   Map("" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES)))
      }

    def genSelfEmploymentPeriodUpdate(nullFinancials: Boolean = false): Gen[SelfEmploymentPeriodUpdate] =
      (for {
        incomes <- Gen.option(genIncomes)
        expenses <- Gen.option(genExpenses)
      } yield {
        SelfEmploymentPeriodUpdate(incomes, expenses, None)
      }) retryUntil { period =>
        if (nullFinancials) period.incomes.isEmpty && period.expenses.isEmpty
        else period.incomes.exists(_.hasIncomes) || period.expenses.exists(_.hasExpenses)
      }

  }
}
