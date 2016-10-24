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

package uk.gov.hmrc.selfassessmentapi.controllers.live.furnishedholidaylettings

import uk.gov.hmrc.play.http.NotImplementedException
import uk.gov.hmrc.selfassessmentapi.controllers.api.SummaryType
import uk.gov.hmrc.selfassessmentapi.controllers.{AnnualSummaryHandler, SourceHandler, SummaryHandler}
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.SourceType.FurnishedHolidayLettings
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.SummaryTypes.{BalancingCharges, Expenses, Incomes, PrivateUseAdjustments}
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.repositories.live.FurnishedHolidayLettingsRepository
import uk.gov.hmrc.selfassessmentapi.repositories.{SourceRepositoryWrapper, SummaryRepositoryWrapper}

object FurnishedHolidayLettingsSourceHandler extends SourceHandler(FurnishedHolidayLetting, FurnishedHolidayLettings.name) {

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case Incomes =>  Some(SummaryHandler(SummaryRepositoryWrapper(FurnishedHolidayLettingsRepository().IncomeRepository), Income, Incomes.name))
      case Expenses => Some(SummaryHandler(SummaryRepositoryWrapper(FurnishedHolidayLettingsRepository().ExpenseRepository), Expense, Expenses.name))
      case BalancingCharges => Some(SummaryHandler(SummaryRepositoryWrapper(FurnishedHolidayLettingsRepository().BalancingChargeRepository), BalancingCharge, BalancingCharges.name))
      case PrivateUseAdjustments => Some(SummaryHandler(SummaryRepositoryWrapper(FurnishedHolidayLettingsRepository().PrivateUseAdjustmentRepository), PrivateUseAdjustment, PrivateUseAdjustments.name))
      case _ => throw new NotImplementedException(s"${FurnishedHolidayLettings.name} ${summaryType.name} is not implemented")
    }
  }

  override def annualSummaryHandler(annualSummaryType: AnnualSummaryType): Option[AnnualSummaryHandler[_]] = {
    annualSummaryType match {
      case _ => throw new NotImplementedException(s"${FurnishedHolidayLettings.name} ${annualSummaryType.name} is not implemented")
    }
  }

  override val repository = SourceRepositoryWrapper(FurnishedHolidayLettingsRepository())
}
