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

package uk.gov.hmrc.selfassessmentapi.repositories.sandbox

import play.api.libs.json.Json._
import play.api.libs.json.Writes
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.repositories.AnnualSummaryRepository

import scala.concurrent.Future

trait SandboxAnnualSummaryRepository[T] extends AnnualSummaryRepository[T] {
  def example: T

  implicit val writes: Writes[T]

  private def exampleJson() = toJson(example)

  override def find(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId): Future[Option[T]] = {
    Future.successful(Some(example))
  }

  override def createOrUpdate(saUtr: SaUtr, taxYear: TaxYear, sourceId: SourceId, summary: T): Future[Boolean] = {
    Future.successful(true)
  }
}
