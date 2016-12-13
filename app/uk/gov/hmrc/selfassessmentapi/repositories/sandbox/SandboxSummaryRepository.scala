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
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, SummaryId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.repositories.{JsonItem, SummaryRepository}

import scala.concurrent.Future

trait SandboxSummaryRepository[T] extends SummaryRepository[T] {

  def example(id: Option[SummaryId]): T

  implicit val writes: Writes[T]

  private def exampleJson(summaryId: SummaryId) = toJson(example(Some(summaryId)))

  private def generateId: String = BSONObjectID.generate.stringify

  override def create(nino: Nino, taxYear: TaxYear, sourceId: SourceId, income: T) = Future.successful(Some(generateId))

  override def delete(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Boolean] = Future.successful(true)

  override def findById(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId): Future[Option[T]] =
    Future.successful(Some(example(Some(id))))

  override def listAsJsonItem(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Seq[JsonItem]] = {
    def createJsonItem(summaryId: SummaryId) = JsonItem(summaryId, exampleJson(summaryId))
    Future.successful(
      Seq(
        createJsonItem(generateId),
        createJsonItem(generateId),
        createJsonItem(generateId),
        createJsonItem(generateId),
        createJsonItem(generateId)
      )
    )
  }

  override def update(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, income: T) = Future.successful(true)

  override def list(nino: Nino, taxYear: TaxYear, sourceId: SourceId): Future[Option[Seq[T]]] = ???
}
