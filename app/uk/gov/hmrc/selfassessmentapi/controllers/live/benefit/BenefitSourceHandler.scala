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

package uk.gov.hmrc.selfassessmentapi.controllers.live.benefit

import uk.gov.hmrc.play.http.NotImplementedException
import uk.gov.hmrc.selfassessmentapi.controllers.api.{AnnualSummaryType, SummaryType}
import uk.gov.hmrc.selfassessmentapi.controllers.api.benefit.SourceType.Benefits
import uk.gov.hmrc.selfassessmentapi.controllers.api.benefit.SummaryTypes.Incomes
import uk.gov.hmrc.selfassessmentapi.controllers.api.benefit.{Benefit, Income}
import uk.gov.hmrc.selfassessmentapi.controllers.{AnnualSummaryHandler, SourceHandler, SummaryHandler}
import uk.gov.hmrc.selfassessmentapi.repositories.live.BenefitsRepository
import uk.gov.hmrc.selfassessmentapi.repositories.{SourceRepositoryWrapper, SummaryRepositoryWrapper}

object BenefitSourceHandler extends SourceHandler(Benefit, Benefits.name) {

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case Incomes => Some(SummaryHandler(SummaryRepositoryWrapper(BenefitsRepository().BenefitRepository), Income, Benefits.name))
      case _ => throw new NotImplementedException(s"${Benefits.name} ${summaryType.name} is not implemented")
    }
  }

  override def annualSummaryHandler(annualSummaryType: AnnualSummaryType): Option[AnnualSummaryHandler[_]] = {
    annualSummaryType match {
      case _ => throw new NotImplementedException(s"${Benefits.name} ${annualSummaryType.name} is not implemented")
    }
  }

  override val repository = SourceRepositoryWrapper(BenefitsRepository())
}
