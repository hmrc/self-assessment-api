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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox.benefit

import uk.gov.hmrc.selfassessmentapi.controllers.api.SourceTypes.Benefits
import uk.gov.hmrc.selfassessmentapi.controllers.api.benefit.SummaryTypes.Incomes
import uk.gov.hmrc.selfassessmentapi.controllers.api.benefit.{Benefit, _}
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SummaryType, _}
import uk.gov.hmrc.selfassessmentapi.controllers.{AnnualSummaryHandler, SourceHandler, SummaryHandler}
import uk.gov.hmrc.selfassessmentapi.repositories.sandbox.{SandboxSourceRepository, SandboxSummaryRepository}

object BenefitSourceHandler extends SourceHandler(Benefit, Benefits.name) {
  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] =
    summaryType match {
      case Incomes => Some(SummaryHandler(new SandboxSummaryRepository[Income] {
        override def example(id: Option[SummaryId]) = Income.example(id)
        override implicit val writes = Income.writes
      }, Income, Incomes.name))
      case _ => None
    }

  override def annualSummaryHandler(annualSummaryType: AnnualSummaryType): Option[AnnualSummaryHandler[_]] = None

  override val repository = new SandboxSourceRepository[Benefit] {
    override implicit val writes = Benefit.writes
    override def example(id: SourceId) = Benefit.example().copy(id = Some(id))
  }
}
