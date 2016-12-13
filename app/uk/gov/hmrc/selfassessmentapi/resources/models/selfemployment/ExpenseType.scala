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

import uk.gov.hmrc.selfassessmentapi.resources.models.EnumJson

object ExpenseType extends Enumeration {
  type ExpenseType = Value

  val CostOfGoodsBought = Value("costOfGoodsBought")
  val CISPaymentsToSubcontractors = Value("cisPaymentsToSubcontractors")
  val StaffCosts = Value("staffCosts")
  val TravelCosts = Value("travelCosts")
  val PremisesRunningCosts = Value("premisesRunningCosts")
  val MaintenanceCosts = Value("maintenanceCosts")
  val AdminCosts = Value("adminCosts")
  val AdvertisingCosts = Value("advertisingCosts")
  val Interest = Value("interest")
  val FinancialCharges = Value("financialCharges")
  val BadDebt = Value("badDebt")
  val ProfessionalFees = Value("professionalFees")
  val Depreciation = Value("depreciation")
  val Other = Value("other")

  implicit val seExpenseTypes = EnumJson.enumFormat(ExpenseType, Some("Self Employment Expense type is invalid"))
}
