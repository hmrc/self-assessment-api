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

package uk.gov.hmrc.selfassessmentapi.controllers.sandbox.dividend

import uk.gov.hmrc.selfassessmentapi.controllers.api.dividend.SourceType.Dividends
import uk.gov.hmrc.selfassessmentapi.controllers.api.dividend.SummaryTypes._
import uk.gov.hmrc.selfassessmentapi.controllers.api.dividend._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, SummaryType}
import uk.gov.hmrc.selfassessmentapi.controllers.{SourceHandler, SummaryHandler}
import uk.gov.hmrc.selfassessmentapi.repositories.sandbox.{SandboxSourceRepository, SandboxSummaryRepository}

object DividendSourceHandler extends SourceHandler(Dividend, Dividends.name) {

  override def summaryHandler(summaryType: SummaryType): Option[SummaryHandler[_]] = {
    summaryType match {
      case Incomes =>
        Some(SummaryHandler(new SandboxSummaryRepository[DividendIncome] {
          override def example(id: Option[SummaryId]) = DividendIncome.example(id)
          override implicit val writes = DividendIncome.writes
        }, DividendIncome, Incomes.name))
      case _ => None
    }
  }

  override val repository = new SandboxSourceRepository[Dividend] {
    override implicit val writes = Dividend.writes
    override def example(id: SourceId) = Dividend.example().copy(id = Some(id))
  }
}
