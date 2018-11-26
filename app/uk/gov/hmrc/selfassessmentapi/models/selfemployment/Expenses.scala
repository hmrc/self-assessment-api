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

import play.api.libs.json.{Json, Reads, Writes}
import uk.gov.hmrc.selfassessmentapi.models.{Expense, ExpenseNegativeOrPositive, ExpenseProfessionalFees}

case class Expenses(costOfGoodsBought: Option[ExpenseNegativeOrPositive] = None,
                    cisPaymentsToSubcontractors: Option[Expense] = None,
                    staffCosts: Option[Expense] = None,
                    travelCosts: Option[Expense] = None,
                    premisesRunningCosts: Option[ExpenseNegativeOrPositive] = None,
                    maintenanceCosts: Option[ExpenseNegativeOrPositive] = None,
                    adminCosts: Option[Expense] = None,
                    advertisingCosts: Option[Expense] = None,
                    businessEntertainmentCosts: Option[Expense] = None,
                    interest: Option[ExpenseNegativeOrPositive] = None,
                    financialCharges: Option[ExpenseNegativeOrPositive] = None,
                    badDebt: Option[Expense] = None,
                    professionalFees: Option[ExpenseProfessionalFees] = None,
                    depreciation: Option[ExpenseNegativeOrPositive] = None,
                    other: Option[Expense] = None) {

  def hasExpenses: Boolean =
    costOfGoodsBought.isDefined ||
      cisPaymentsToSubcontractors.isDefined ||
      staffCosts.isDefined ||
      travelCosts.isDefined ||
      premisesRunningCosts.isDefined ||
      maintenanceCosts.isDefined ||
      adminCosts.isDefined ||
      advertisingCosts.isDefined ||
      businessEntertainmentCosts.isDefined ||
      interest.isDefined ||
      financialCharges.isDefined ||
      badDebt.isDefined ||
      professionalFees.isDefined ||
      depreciation.isDefined ||
      other.isDefined
}

object Expenses {
  implicit val writes: Writes[Expenses] = Json.writes[Expenses]
  implicit val reads: Reads[Expenses] = Json.reads[Expenses]
}
