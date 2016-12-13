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

import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.Generators.amountGen
import uk.gov.hmrc.selfassessmentapi.controllers.api.ErrorCode.{apply => _, _}
import uk.gov.hmrc.selfassessmentapi.controllers.api.JsonSpec
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.ExpenseType._

class ExpenseSpec extends JsonSpec with GeneratorDrivenPropertyChecks {

  "format" should {

    val genValidExpense = for {
      expenseType <- Gen.oneOf(ExpenseType.values.toList)
      amount <- amountGen(1000, 5000)
      disallowableAmount <- { // wrap if in a block to get proper indentation
        if (expenseType == ExpenseType.Depreciation) Gen.const(amount)
        else amountGen(0, amount - 1)
      }
    } yield Expense(`type` = expenseType, amount = amount, disallowableAmount = disallowableAmount)

    "round trip Expense json" in forAll(genValidExpense) { expense =>
      roundTripJson(expense)
    }
  }

  "validate" should {
    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach {
        testAmount =>
          val seExpense = Expense(`type` = CISPaymentsToSubcontractors, amount = testAmount, disallowableAmount = 0)
          assertValidationErrorWithCode(seExpense,
                                         "/amount", INVALID_MONETARY_AMOUNT)
      }
    }

    "reject negative monetary amounts" in {
      Seq(BigDecimal(-1000.12), BigDecimal(-10.12)).foreach { testAmount =>
        val seExpense = Expense(`type` = CISPaymentsToSubcontractors, amount = testAmount, disallowableAmount = 0)
        assertValidationErrorWithCode(seExpense,
                                       "/amount", INVALID_MONETARY_AMOUNT)
      }
    }

    "reject negative amount" in {
      val seExpense = Expense(`type` = CISPaymentsToSubcontractors, amount = BigDecimal(-1000.12), disallowableAmount = 0)
      assertValidationErrorWithCode(seExpense,
                                     "/amount", INVALID_MONETARY_AMOUNT)
    }

    "reject invalid Expense category" in {
      val json = """
          |{ "type": "BAZ",
          |"amount" : 10000.45,
          |"disallowableAmount": 0
          |}
        """.stripMargin

      assertValidationErrorsWithCode[Expense](Json.parse(json), Map("/type" -> INVALID_VALUE))
    }

    val genExpenseWithInvalidDisallowableAmount = for {
      expenseType <- Gen.oneOf((ExpenseType.values - ExpenseType.Depreciation).toList)
      amount <- amountGen(1000, 5000)
      disallowableAmount <- amountGen(amount + 1, amount + 1000)
    } yield Expense(`type` = expenseType, amount = amount, disallowableAmount = disallowableAmount)

    "reject Expense with disallowable amount greater than total amount" in forAll(
      genExpenseWithInvalidDisallowableAmount) { expense =>
      assertValidationErrorWithCode(expense,
                                     "", INVALID_DISALLOWABLE_AMOUNT)
    }

    val genInvalidDepreciationExpense = for {
      amount <- amountGen(1000, 5000)
      disallowableAmount <- amountGen(0, amount - 1)
    } yield
      Expense(`type` = ExpenseType.Depreciation, amount = amount, disallowableAmount = disallowableAmount)

    "reject Depreciation Expense where disallowable amount is different than total amount" in forAll(
      genInvalidDepreciationExpense) { expense =>
      assertValidationErrorWithCode(expense,
                                     "", DEPRECIATION_DISALLOWABLE_AMOUNT)
    }
  }
}
