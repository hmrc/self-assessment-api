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

package uk.gov.hmrc.selfassessmentapi.repositories

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers.api._

import scala.concurrent.Future

trait AnnualSummaryRepository[T] {
  def find(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[T]]

  def createOrUpdate(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, summary: T): Future[Boolean]
}

case class AnnualSummaryRepositoryWrapper[T](annualSummaryRepository: AnnualSummaryRepository[T]) extends AnnualSummaryRepository[T] {
  private val selfAssessmentRepository = SelfAssessmentRepository()

  override def find(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[T]] = {
    annualSummaryRepository.find(saUtr, taxYear, sourceId)
  }

  override def createOrUpdate(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, summary: T): Future[Boolean] = {
    selfAssessmentRepository.touch(saUtr, taxYear)
    annualSummaryRepository.createOrUpdate(saUtr, taxYear, sourceId, summary)
  }
}
