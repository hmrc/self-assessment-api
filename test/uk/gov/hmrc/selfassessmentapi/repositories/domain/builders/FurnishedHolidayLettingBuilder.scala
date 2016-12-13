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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.builders

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.TestUtils._
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.ExpenseType._
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.PropertyLocationType
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.PropertyLocationType.PropertyLocationType
import uk.gov.hmrc.selfassessmentapi.controllers.util.NinoGenerator
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

case class FurnishedHolidayLettingBuilder(capitalAllowance: BigDecimal = 0,
                                          location: PropertyLocationType = PropertyLocationType.UK) {
  import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings._

  private val objectID = BSONObjectID.generate

  private var furnishedHolidayLetting: FurnishedHolidayLettings =
    FurnishedHolidayLettings(objectID, objectID.stringify, NinoGenerator().nextNino(), taxYear, now, now, location, allowances = Some(Allowances(Some(capitalAllowance))))

  def lossBroughtForward(amount: BigDecimal) = {
    furnishedHolidayLetting = furnishedHolidayLetting.copy(adjustments = Some(Adjustments(lossBroughtForward = Some(amount))))
    this
  }

  def incomes(incomes: BigDecimal*) = {
    furnishedHolidayLetting = furnishedHolidayLetting.copy(incomes = incomes.map (FurnishedHolidayLettingsIncomeSummary("", _)))
    this
  }

  private def expenses(expenses: (ExpenseType, BigDecimal)*) = {
    furnishedHolidayLetting = furnishedHolidayLetting.copy(expenses = furnishedHolidayLetting.expenses ++ expenses.map (expense => FurnishedHolidayLettingsExpenseSummary("", expense._1, expense._2)))
    this
  }

  def withPremisesRunningCosts(costs: BigDecimal*) = {
     expenses(costs.map((PremisesRunningCosts, _)):_*)
  }

  def withFinancialCosts(financialCosts: BigDecimal*) = {
     expenses(financialCosts.map((FinancialCosts, _)):_*)
  }

  def withProfessionalFees(professionalFees: BigDecimal*) = {
     expenses(professionalFees.map((ProfessionalFees, _)):_*)
  }

  def withOtherExpenses(otherExpenses: BigDecimal*) = {
     expenses(otherExpenses.map((Other, _)):_*)
  }

  def balancingCharges(amounts: BigDecimal*) = {
    furnishedHolidayLetting = furnishedHolidayLetting.copy(balancingCharges = amounts.map (FurnishedHolidayLettingsBalancingChargeSummary("", _)))
    this
  }

  def privateUseAdjustments(amounts: BigDecimal*) = {
    furnishedHolidayLetting = furnishedHolidayLetting.copy(privateUseAdjustment = amounts.map (FurnishedHolidayLettingsPrivateUseAdjustmentSummary("", _)))
    this
  }

  def create() = furnishedHolidayLetting
}
