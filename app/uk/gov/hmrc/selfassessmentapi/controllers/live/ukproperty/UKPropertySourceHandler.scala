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

package uk.gov.hmrc.selfassessmentapi.controllers.live.ukproperty

import uk.gov.hmrc.play.http.NotImplementedException
import uk.gov.hmrc.selfassessmentapi.controllers.api.SummaryType
import uk.gov.hmrc.selfassessmentapi.controllers.{AnnualSummaryHandler, SourceHandler, SummaryHandler}
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.SourceType.UKProperties
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.SummaryTypes._
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty._
import uk.gov.hmrc.selfassessmentapi.repositories.live.UKPropertiesRepository
import uk.gov.hmrc.selfassessmentapi.repositories.{SourceRepositoryWrapper, SummaryRepositoryWrapper}

object UKPropertySourceHandler extends SourceHandler(UKProperty, UKProperties.name) {

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case Incomes =>  Some(SummaryHandler(SummaryRepositoryWrapper(UKPropertiesRepository().IncomeRepository), Income, Incomes.name))
      case Expenses => Some(SummaryHandler(SummaryRepositoryWrapper(UKPropertiesRepository().ExpenseRepository), Expense, Expenses.name))
      case BalancingCharges => Some(SummaryHandler(SummaryRepositoryWrapper(UKPropertiesRepository().BalancingChargeRepository), BalancingCharge, BalancingCharges.name))
      case PrivateUseAdjustments => Some(SummaryHandler(SummaryRepositoryWrapper(UKPropertiesRepository().PrivateUseAdjustmentRepository), PrivateUseAdjustment, PrivateUseAdjustments.name))
      case TaxesPaid => Some(SummaryHandler(SummaryRepositoryWrapper(UKPropertiesRepository().TaxPaidRepository), TaxPaid, TaxesPaid.name))
      case _ => throw new NotImplementedException(s"${UKProperties.name} ${summaryType.name} is not implemented")
    }
  }

  override def annualSummaryHandler(annualSummaryType: AnnualSummaryType): Option[AnnualSummaryHandler[_]] = {
    annualSummaryType match {
      case _ => throw new NotImplementedException(s"${UKProperties.name} ${annualSummaryType.name} is not implemented")
    }
  }

  override val repository = SourceRepositoryWrapper(UKPropertiesRepository())
}
