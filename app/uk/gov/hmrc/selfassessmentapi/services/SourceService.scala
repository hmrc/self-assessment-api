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

package uk.gov.hmrc.selfassessmentapi.services

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, TaxYear}

import scala.concurrent.Future

trait SourceService[T] {

  def create(nino: Nino, taxYear: TaxYear, source: T) : Future[SourceId]

  def findById(nino: Nino, taxYear: TaxYear, sourceId: SourceId) : Future[Option[T]]

  def list(nino: Nino, taxYear: TaxYear) : Future[Seq[T]]

  def update(nino: Nino, taxYear: TaxYear, sourceId: SourceId, source: T): Future[Boolean]

  def delete(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Boolean]
}
