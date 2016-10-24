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

package uk.gov.hmrc.selfassessmentapi.controllers.live.employment

import uk.gov.hmrc.play.http.NotImplementedException
import uk.gov.hmrc.selfassessmentapi.controllers.api.{AnnualSummaryType, SourceTypes, SummaryType}
import uk.gov.hmrc.selfassessmentapi.controllers.{AnnualSummaryHandler, SourceHandler, SummaryHandler}
import SourceTypes._
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment._
import uk.gov.hmrc.selfassessmentapi.controllers.api.employment.SummaryTypes.{Benefits, Expenses, Incomes, UkTaxesPaid}
import uk.gov.hmrc.selfassessmentapi.repositories.live.EmploymentRepository
import uk.gov.hmrc.selfassessmentapi.repositories.{SourceRepository, SourceRepositoryWrapper, SummaryRepositoryWrapper}

object EmploymentSourceHandler extends SourceHandler(Employment, Employments.name) {

  override val repository: SourceRepository[Employment] = SourceRepositoryWrapper(EmploymentRepository())

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case Incomes => Some(SummaryHandler(SummaryRepositoryWrapper(EmploymentRepository().IncomeRepository), Income, Incomes.name))
      case Expenses => Some(SummaryHandler(SummaryRepositoryWrapper(EmploymentRepository().ExpenseRepository), Expense, Expenses.name))
      case Benefits => Some(SummaryHandler(SummaryRepositoryWrapper(EmploymentRepository().BenefitRepository), Benefit, Benefits.name))
      case UkTaxesPaid => Some(SummaryHandler(SummaryRepositoryWrapper(EmploymentRepository().UkTaxPaidRepository), UkTaxPaid, UkTaxesPaid.name))
      case _ => throw new NotImplementedException(s"${Employments.name} ${summaryType.name} is not implemented")
    }
  }

  override def annualSummaryHandler(annualSummaryType: AnnualSummaryType): Option[AnnualSummaryHandler[_]] = {
    annualSummaryType match {
      case _ => throw new NotImplementedException(s"${Employments.name} ${annualSummaryType.name} is not implemented")
    }
  }
}
