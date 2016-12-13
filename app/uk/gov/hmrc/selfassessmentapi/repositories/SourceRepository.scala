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
import uk.gov.hmrc.selfassessmentapi.controllers.api._

import scala.concurrent.Future

trait SourceRepository[T] {

  def create(nino: Nino, taxYear: TaxYear, source: T): Future[SourceId]

  def findById(nino: Nino, taxYear: TaxYear, id: SourceId): Future[Option[T]]

  def update(nino: Nino, taxYear: TaxYear, id: SourceId, source: T): Future[Boolean]

  def delete(nino: Nino, taxYear: TaxYear, id: SourceId): Future[Boolean]

  def delete(nino: Nino, taxYear: TaxYear): Future[Boolean]

  def list(nino: Nino, taxYear: TaxYear): Future[Seq[T]]

  def listAsJsonItem(nino: Nino, taxYear: TaxYear): Future[Seq[JsonItem]]
}

//todo feel free to implement this functionality in more scalaish way
case class SourceRepositoryWrapper[T](private val target: SourceRepository[T]) extends SourceRepository[T] {

  lazy val selfAssessmentRepository = SelfAssessmentRepository()

  override def create(nino: Nino, taxYear: TaxYear, source: T) : Future[SourceId] = {
    selfAssessmentRepository.touch(nino, taxYear)
    target.create(nino, taxYear, source)
  }

  override def update(nino: Nino, taxYear: TaxYear, id: SourceId, source: T): Future[Boolean] = {
    selfAssessmentRepository.touch(nino, taxYear)
    target.update(nino, taxYear, id, source)
  }

  override def findById(nino: Nino, taxYear: TaxYear, id: SourceId): Future[Option[T]] = target.findById(nino, taxYear, id)

  override def delete(nino: Nino, taxYear: TaxYear, id: SourceId): Future[Boolean] = {
    selfAssessmentRepository.touch(nino, taxYear)
    target.delete(nino, taxYear, id)
  }

  override def delete(nino: Nino, taxYear: TaxYear): Future[Boolean] = {
    selfAssessmentRepository.touch(nino, taxYear)
    target.delete(nino, taxYear)
  }

  override def list(nino: Nino, taxYear: TaxYear): Future[Seq[T]] = target.list(nino, taxYear)

  override def listAsJsonItem(nino: Nino, taxYear: TaxYear): Future[Seq[JsonItem]] = target.listAsJsonItem(nino, taxYear)
}