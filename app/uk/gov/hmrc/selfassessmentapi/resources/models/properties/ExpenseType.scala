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

package uk.gov.hmrc.selfassessmentapi.resources.models.properties

import uk.gov.hmrc.selfassessmentapi.resources.models.EnumJson

object ExpenseType extends Enumeration {
  type ExpenseType = Value

  val PremisesRunningCosts = Value("premisesRunningCosts")
  val RepairsAndMaintenance = Value("repairsAndMaintenance")
  val FinancialCosts = Value("financialCosts")
  val ProfessionalFees = Value("professionalFees")
  val CostOfServices = Value("costOfServices")
  val Other = Value("other")

  implicit val types = EnumJson.enumFormat(ExpenseType, Some("UK Property Expense type is invalid"))
}
