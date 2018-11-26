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

package uk.gov.hmrc.r1.selfassessmentapi.models.selfemployment

import uk.gov.hmrc.r2.selfassessmentapi.models.{ErrorCode, Expense, ExpenseNegativeOrPositive, ExpenseProfessionalFees}
import uk.gov.hmrc.r2.selfassessmentapi.resources.JsonSpec

class ExpensesSpec extends JsonSpec {

  "Expenses" should {

    "round trip in" in
      roundTripJson(Expenses(depreciation = Some(ExpenseNegativeOrPositive(200, Some(200))), badDebt = Some(Expense(200, Some(100)))))

    "accept Expenses with depreciation expense where disallowable amount is not defined" in
      roundTripJson(Expenses(depreciation = Some(ExpenseNegativeOrPositive(200, None))))

    "accept full Expenses" in
      roundTripJson(Expenses(
        costOfGoodsBought = Some(ExpenseNegativeOrPositive(200, Some(200))),
        cisPaymentsToSubcontractors = Some(Expense(200, Some(100))),
        staffCosts = Some(Expense(200, Some(100))),
        travelCosts = Some(Expense(200, Some(100))),
        premisesRunningCosts = Some(ExpenseNegativeOrPositive(200, Some(200))),
        maintenanceCosts = Some(ExpenseNegativeOrPositive(200, Some(200))),
        adminCosts = Some(Expense(200, Some(100))),
        advertisingCosts = Some(Expense(200, Some(100))),
        businessEntertainmentCosts = Some(Expense(200, Some(100))),
        interest = Some(ExpenseNegativeOrPositive(200, Some(200))),
        financialCharges = Some(ExpenseNegativeOrPositive(200, Some(200))),
        badDebt = Some(Expense(200, Some(100))),
        professionalFees = Some(ExpenseProfessionalFees(200, Some(200))),
        depreciation = Some(ExpenseNegativeOrPositive(200, Some(200))),
        other = Some(Expense(200, Some(100)))
      ))

    "accept full Expenses where disallowableAmount is not defined" in
      roundTripJson(Expenses(
        costOfGoodsBought = Some(ExpenseNegativeOrPositive(200, None)),
        cisPaymentsToSubcontractors = Some(Expense(200, None)),
        staffCosts = Some(Expense(200, None)),
        travelCosts = Some(Expense(200, None)),
        premisesRunningCosts = Some(ExpenseNegativeOrPositive(200, None)),
        maintenanceCosts = Some(ExpenseNegativeOrPositive(200, None)),
        adminCosts = Some(Expense(200, None)),
        advertisingCosts = Some(Expense(200, None)),
        businessEntertainmentCosts = Some(Expense(200, None)),
        interest = Some(ExpenseNegativeOrPositive(200, None)),
        financialCharges = Some(ExpenseNegativeOrPositive(200, None)),
        badDebt = Some(Expense(200, None)),
        professionalFees = Some(ExpenseProfessionalFees(200, None)),
        depreciation = Some(ExpenseNegativeOrPositive(200, None)),
        other = Some(Expense(200, None))
      ))

    "reject an invalid amount for costOfGoodsBought" in
    assertValidationErrorWithCode(Expenses(
      costOfGoodsBought = Some(ExpenseNegativeOrPositive(BigDecimal(-100000000000.00), None)),
      cisPaymentsToSubcontractors = Some(Expense(200, None)),
      staffCosts = Some(Expense(200, None)),
      travelCosts = Some(Expense(200, None)),
      premisesRunningCosts = Some(ExpenseNegativeOrPositive(200, None)),
      maintenanceCosts = Some(ExpenseNegativeOrPositive(200, None)),
      adminCosts = Some(Expense(200, None)),
      advertisingCosts = Some(Expense(200, None)),
      businessEntertainmentCosts = Some(Expense(200, None)),
      interest = Some(ExpenseNegativeOrPositive(200, None)),
      financialCharges = Some(ExpenseNegativeOrPositive(200, None)),
      badDebt = Some(Expense(200, None)),
      professionalFees = Some(ExpenseProfessionalFees(200, None)),
      depreciation = Some(ExpenseNegativeOrPositive(200, None)),
      other = Some(Expense(200, None))), "/costOfGoodsBought/amount", ErrorCode.INVALID_MONETARY_AMOUNT)

  }

}
