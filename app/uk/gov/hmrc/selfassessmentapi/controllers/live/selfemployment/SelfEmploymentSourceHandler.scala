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

package uk.gov.hmrc.selfassessmentapi.controllers.live.selfemployment

import play.api.libs.json.JsValue
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.play.http.NotImplementedException
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.api.SummaryType
import uk.gov.hmrc.selfassessmentapi.controllers._
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SourceType.SelfEmployments
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment.SummaryTypes.{BalancingCharges, Expenses, GoodsAndServicesOwnUses, Incomes}
import uk.gov.hmrc.selfassessmentapi.controllers.api.selfemployment._
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.repositories.{SourceRepositoryWrapper, SummaryRepositoryWrapper, AnnualSummaryRepositoryWrapper}
import uk.gov.hmrc.selfassessmentapi.repositories.live.SelfEmploymentRepository

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object SelfEmploymentSourceHandler extends SourceHandler(SelfEmployment, SelfEmployments.name) {

  private val limitSelfEmployments: Boolean = AppContext.sourceLimits.flatMap(_.getBoolean("self-employments")).getOrElse(false)

  override def create(saUtr: SaUtr, taxYear: TaxYear, jsValue: JsValue): Either[ErrorResult, Future[String]] = {
    val numSelfEmployments = Await.result(repository.list(saUtr, taxYear), Duration.Inf).size

    if (limitSelfEmployments && numSelfEmployments >= 1) {
      Left(GenericErrorResult(s"You may only create a maximum of 1 self-employments. Current number of self-employments: $numSelfEmployments"))
    } else {
      super.create(saUtr, taxYear, jsValue)
    }
  }

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case Incomes =>  Some(SummaryHandler(SummaryRepositoryWrapper(SelfEmploymentRepository().IncomeRepository), Income, Incomes.name))
      case Expenses => Some(SummaryHandler(SummaryRepositoryWrapper(SelfEmploymentRepository().ExpenseRepository), Expense, Expenses.name))
      case BalancingCharges => Some(SummaryHandler(SummaryRepositoryWrapper(SelfEmploymentRepository().BalancingChargeRepository), BalancingCharge, BalancingCharges.name))
      case GoodsAndServicesOwnUses => Some(SummaryHandler(SummaryRepositoryWrapper(SelfEmploymentRepository().GoodsAndServicesOwnUseRepository), GoodsAndServicesOwnUse, GoodsAndServicesOwnUses.name))
      case _ => throw new NotImplementedException(s"${SelfEmployments.name} ${summaryType.name} is not implemented")
    }
  }

  override def annualSummaryHandler(annualSummaryType: AnnualSummaryType): Option[AnnualSummaryHandler[_]] = {
    annualSummaryType match {
      case AnnualSummaryType.Adjustments => Some(AnnualSummaryHandler(AnnualSummaryRepositoryWrapper(SelfEmploymentRepository().AdjustmentsRepository), Adjustments))
      case AnnualSummaryType.Allowances => Some(AnnualSummaryHandler(AnnualSummaryRepositoryWrapper(SelfEmploymentRepository().AllowancesRepository), Allowances))
      case _ => throw new NotImplementedException(s"${SelfEmployments.name} ${annualSummaryType.name} is not implemented")
    }
  }

  override val repository = SourceRepositoryWrapper(SelfEmploymentRepository())
}
