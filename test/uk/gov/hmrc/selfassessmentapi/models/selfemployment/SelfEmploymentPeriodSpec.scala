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

import org.joda.time.LocalDate
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.models.Generators._
import uk.gov.hmrc.selfassessmentapi.models.{ErrorCode, SimpleIncome}
import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec

class SelfEmploymentPeriodSpec extends JsonSpec with GeneratorDrivenPropertyChecks {

  "SelfEmploymentPeriod" should {
    "round trip" in forAll(genSelfEmploymentPeriod())(roundTripJson(_))

    "return a INVALID_PERIOD error when using a period with a 'from' date that becomes before the 'to' date" in
      forAll(genSelfEmploymentPeriod(invalidPeriod = true)) { period =>
        assertValidationErrorsWithCode[SelfEmploymentPeriod](Json.toJson(period),
                                                             Map("" -> Seq(ErrorCode.INVALID_PERIOD)))
      }

    "return a NO_INCOMES_AND_EXPENSES error when incomes and expenses are not supplied" in
      forAll(genSelfEmploymentPeriod(nullFinancials = true)) { period =>
        assertValidationErrorsWithCode[SelfEmploymentPeriod](Json.toJson(period),
                                                             Map("" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES)))
      }

    "return a INVALID_PERIOD and NO_INCOMES_AND_EXPENSES errors when the from and to dates are invalid and incomes and expenses are not supplied" in
      forAll(genSelfEmploymentPeriod(invalidPeriod = true, nullFinancials = true)) { period =>
        assertValidationErrorsWithCode[SelfEmploymentPeriod](
          Json.toJson(period),
          Map("" -> Seq(ErrorCode.NO_INCOMES_AND_EXPENSES, ErrorCode.INVALID_PERIOD)))
      }

    "return an error when provided with an empty json body" in
      assertValidationErrorsWithMessage[SelfEmploymentPeriod](Json.parse("{}"),
                                                              Map("/from" -> Seq("error.path.missing"),
                                                                  "/to" -> Seq("error.path.missing")))

    "pass if the from date is equal to the end date" in {
      val period = SelfEmploymentPeriod(id = None,
                                        from = LocalDate.parse("2017-04-01"),
                                        to = LocalDate.parse("2017-04-01"),
                                        incomes = Some(Incomes(turnover = Some(SimpleIncome(0)))),
                                        expenses = None,
                                        consolidatedExpenses = None)
      assertValidationPasses(period)
    }
  }

  def genSelfEmploymentPeriod(invalidPeriod: Boolean = false,
                              nullFinancials: Boolean = false): Gen[SelfEmploymentPeriod] =
    (for {
      incomes <- Gen.option(genIncomes)
      expenses <- Gen.option(genExpenses)
    } yield {
      val from = LocalDate.now()
      val to = from.plusDays(1)
      SelfEmploymentPeriod(None, if (invalidPeriod) to else from, if (invalidPeriod) from else to, incomes, expenses, None)
    }) retryUntil { period =>
      if (nullFinancials) period.incomes.isEmpty && period.expenses.isEmpty
      else period.incomes.exists(_.hasIncomes) || period.expenses.exists(_.hasExpenses) || period.consolidatedExpenses.isDefined
    }

}
