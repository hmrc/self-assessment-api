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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox.selfemployment

import play.api.libs.json.Writes
import uk.gov.hmrc.selfassessmentapi.controllers.api.SummaryType
import uk.gov.hmrc.selfassessmentapi.controllers.{AnnualSummaryHandler, SourceHandler, SummaryHandler}
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SummaryTypes._
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.repositories.sandbox.{SandboxAnnualSummaryRepository, SandboxSourceRepository, SandboxSummaryRepository}

object SelfEmploymentSourceHandler extends SourceHandler(SelfEmployment, SelfEmployments.name) {

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case Incomes => Some(SummaryHandler(new SandboxSummaryRepository[Income] {
        override def example(id: Option[SummaryId]) = Income.example(id)
        override implicit val writes: Writes[Income] = Income.writes
      }, Income, Incomes.name))
      case Expenses => Some(SummaryHandler(new SandboxSummaryRepository[Expense] {
        override def example(id: Option[SummaryId]): Expense = Expense.example(id)
        override implicit val writes: Writes[Expense] = Expense.writes
      }, Expense, Expenses.name))
      case BalancingCharges => Some(SummaryHandler(new SandboxSummaryRepository[BalancingCharge] {
        override def example(id: Option[SummaryId]) = BalancingCharge.example(id)
        override implicit val writes = BalancingCharge.writes
      }, BalancingCharge, BalancingCharges.name))
      case GoodsAndServicesOwnUses => Some(SummaryHandler(new SandboxSummaryRepository[GoodsAndServicesOwnUse] {
        override def example(id: Option[SummaryId]) = GoodsAndServicesOwnUse.example(id)
        override implicit val writes = GoodsAndServicesOwnUse.writes
      }, GoodsAndServicesOwnUse, GoodsAndServicesOwnUses.name))
      case _ => None
    }
  }


  override def annualSummaryHandler(annualSummaryType: AnnualSummaryType): Option[AnnualSummaryHandler[_]] = {
    annualSummaryType match {
      case AnnualSummaryType.Adjustments => Some(AnnualSummaryHandler(new SandboxAnnualSummaryRepository[Adjustments] {
        override def example: Adjustments = Adjustments.example(None)
        override implicit val writes: Writes[Adjustments] = Adjustments.writes
      }, Adjustments))
      case AnnualSummaryType.Allowances => Some(AnnualSummaryHandler(new SandboxAnnualSummaryRepository[Allowances] {
        override def example: Allowances = Allowances.example(None)
        override implicit val writes: Writes[Allowances] = Allowances.writes
      }, Allowances))
      case _ => None
    }
  }

  override val repository = new SandboxSourceRepository[SelfEmployment] {
    override implicit val writes = SelfEmployment.writes
    override def example(id: SourceId) = SelfEmployment.example().copy(id = Some(id))
  }
}
