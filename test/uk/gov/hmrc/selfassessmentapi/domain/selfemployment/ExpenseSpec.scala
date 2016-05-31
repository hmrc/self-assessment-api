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

package uk.gov.hmrc.selfassessmentapi.domain.selfemployment

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode.{apply => _, _}
import ExpenseType._
import uk.gov.hmrc.selfassessmentapi.domain.JsonSpec
class ExpenseSpec extends JsonSpec {

  "format" should {
    "round trip Expense json" in {
      ExpenseType.values.foreach {
        cat => roundTripJson(Expense(`type` = cat, amount = BigDecimal(1000.99)))
      }
    }
  }

  "validate" should {
    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach { testAmount =>
        val seExpense = Expense(`type` = CISPayments, amount = testAmount)
        assertValidationError[Expense](
          seExpense,
          Map("/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid self-employment-income")
      }
    }

    "reject negative monetary amounts" in {
      Seq(BigDecimal(-1000.12), BigDecimal(-10.12)).foreach { testAmount =>
        val seExpense = Expense(`type` = CISPayments, amount = testAmount)
        assertValidationError[Expense](
          seExpense,
          Map("/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid self-employment-income")
      }
    }

    "reject negative amount" in {
      val seExpense = Expense(`type` = CISPayments, amount = BigDecimal(-1000.12))
      assertValidationError[Expense](
        seExpense,
        Map("/amount" -> INVALID_MONETARY_AMOUNT), "Expected negative self-employment expense")
    }

    "reject invalid Expense category" in {
      val json = Json.parse(
        """
          |{ "type": "BAZ",
          |"amount" : 10000.45
          |}
        """.
          stripMargin)

      assertValidationError[Expense](
        json,
        Map("/type" -> NO_VALUE_FOUND),
        "Expected expense type not in { CoGBought, CISPayments, StaffCosts, TravelCosts, PremisesRunningCosts, MaintenanceCosts, " +
          "AdminCosts,  AdvertisingCosts, Internet, FinancialCharges, BadDept, ProfessionalFees, Deprecation, Other }")
    }
  }
}
