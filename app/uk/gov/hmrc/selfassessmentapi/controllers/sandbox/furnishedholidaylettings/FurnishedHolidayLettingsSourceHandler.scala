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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox.furnishedholidaylettings

import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceTypes, SummaryType}
import uk.gov.hmrc.selfassessmentapi.controllers.{AnnualSummaryHandler, SourceHandler, SummaryHandler}
import SourceTypes.FurnishedHolidayLettings
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.SummaryTypes._
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.{Income, _}
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.repositories.sandbox.{SandboxSourceRepository, SandboxSummaryRepository}

object FurnishedHolidayLettingsSourceHandler extends SourceHandler(FurnishedHolidayLetting, FurnishedHolidayLettings.name) {

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case PrivateUseAdjustments => Some(SummaryHandler(new SandboxSummaryRepository[PrivateUseAdjustment] {
        override def example(id: Option[SummaryId]) = PrivateUseAdjustment.example(id)
        override implicit val writes = PrivateUseAdjustment.writes
      }, PrivateUseAdjustment, PrivateUseAdjustments.name))
      case Incomes => Some(SummaryHandler(new SandboxSummaryRepository[Income] {
        override def example(id: Option[SummaryId]) = Income.example(id)
        override implicit val writes = Income.writes
      }, Income, Incomes.name))
      case Expenses => Some(SummaryHandler(new SandboxSummaryRepository[Expense] {
        override def example(id: Option[SummaryId]) = Expense.example(id)
        override implicit val writes = Expense.writes
      }, Expense, Expenses.name))
      case BalancingCharges => Some(SummaryHandler(new SandboxSummaryRepository[BalancingCharge] {
        override def example(id: Option[SummaryId]) = BalancingCharge.example(id)
        override implicit val writes = BalancingCharge.writes
      }, BalancingCharge, BalancingCharges.name))
      case _ => None
    }
  }

  override def annualSummaryHandler(annualSummaryType: AnnualSummaryType): Option[AnnualSummaryHandler[_]] = None

  override val repository = new SandboxSourceRepository[FurnishedHolidayLetting] {
    override implicit val writes = FurnishedHolidayLetting.writes
    override def example(id: SourceId) = FurnishedHolidayLetting.example().copy(id = Some(id))
  }
}
