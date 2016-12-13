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

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, TaxYear}

import scala.concurrent.Future

trait SummaryRepository[T] {

  def create(nino: Nino, taxYear: TaxYear, sourceId: SourceId, summary: T): Future[Option[SummaryId]]

  def findById(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[T]]

  def update(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, summary: T): Future[Boolean]

  def delete(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean]

  def list(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[T]]]

  def listAsJsonItem(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]]
}

case class SummaryRepositoryWrapper[T](private val target: SummaryRepository[T]) extends SummaryRepository[T] {

  lazy val selfAssessmentRepository = SelfAssessmentRepository()

  override def create(nino: Nino, taxYear: TaxYear, sourceId: SourceId, income: T): Future[Option[SummaryId]] = {
    selfAssessmentRepository.touch(nino, taxYear)
    target.create(nino, taxYear, sourceId, income)
  }

  override def update(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, summary: T): Future[Boolean] = {
    selfAssessmentRepository.touch(nino, taxYear)
    target.update(nino, taxYear, sourceId, id, summary)
  }

  override def findById(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[T]] =
    target.findById(nino, taxYear, sourceId, id)

  override def delete(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] = {
    selfAssessmentRepository.touch(nino, taxYear)
    target.delete(nino, taxYear, sourceId, id)
  }

  override def list(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[T]]] =
    target.list(nino, taxYear, sourceId)

  override def listAsJsonItem(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] =
    target.listAsJsonItem(nino, taxYear, sourceId)
}