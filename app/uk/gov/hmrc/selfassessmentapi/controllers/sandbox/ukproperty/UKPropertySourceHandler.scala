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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox.ukproperty

import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceTypes, SummaryType}
import uk.gov.hmrc.selfassessmentapi.controllers.{AnnualSummaryHandler, SourceHandler, SummaryHandler}
import SourceTypes.UKProperties
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.SummaryTypes._
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.{Income, _}
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.repositories.sandbox.{SandboxSourceRepository, SandboxSummaryRepository}

object UKPropertySourceHandler extends SourceHandler(UKProperty, UKProperties.name) {

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case Incomes => Some(SummaryHandler(new SandboxSummaryRepository[Income] {
        override def example(id: Option[SummaryId]) = Income.example(id)
        override implicit val writes = Income.writes
      }, Income, Incomes.name))
      case Expenses => Some(SummaryHandler(new SandboxSummaryRepository[Expense] {
        override def example(id: Option[SummaryId]) = Expense.example(id)
        override implicit val writes = Expense.writes
      }, Expense, Expenses.name))
      case TaxesPaid => Some(SummaryHandler(new SandboxSummaryRepository[TaxPaid] {
        override def example(id: Option[SummaryId]) = TaxPaid.example(id)
        override implicit val writes = TaxPaid.writes
      }, TaxPaid, TaxesPaid.name))
      case BalancingCharges => Some(SummaryHandler(new SandboxSummaryRepository[BalancingCharge] {
        override def example(id: Option[SummaryId]) = BalancingCharge.example(id)
        override implicit val writes = BalancingCharge.writes
      }, BalancingCharge, BalancingCharges.name))
      case PrivateUseAdjustments => Some(SummaryHandler(new SandboxSummaryRepository[PrivateUseAdjustment] {
        override def example(id: Option[SummaryId]) = PrivateUseAdjustment.example(id)
        override implicit val writes = PrivateUseAdjustment.writes
      }, PrivateUseAdjustment, PrivateUseAdjustments.name))
      case _ => None
    }
  }

  override def annualSummaryHandler(annualSummaryType: AnnualSummaryType): Option[AnnualSummaryHandler[_]] = None

  override val repository = new SandboxSourceRepository[UKProperty] {
    override implicit val writes = UKProperty.writes
    override def example(id: SourceId) = UKProperty.example().copy(id = Some(id))
  }
}
