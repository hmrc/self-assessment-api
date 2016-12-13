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

import play.api.libs.json.Json.toJson
import play.api.libs.json.Writes
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.controllers.api.{SourceId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.repositories.{JsonItem, SourceRepository}

import scala.concurrent.Future

trait SandboxSourceRepository[T] extends SourceRepository[T] {

  def example(id: SourceId): T
  implicit val writes: Writes[T]

  private def exampleJson(sourceId: SourceId) = toJson(example(sourceId))

  def listAsJsonItem(nino: Nino, taxYear: TaxYear): Future[Seq[JsonItem]] = {
    def createJsonItem(sourceId: SourceId) = JsonItem(sourceId, exampleJson(sourceId))
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

  private def generateId: String = BSONObjectID.generate.stringify

  override def create(nino: Nino, taxYear: TaxYear, source: T) = Future.successful(generateId)

  override def update(nino: Nino, taxYear: TaxYear, id: SourceId, source: T) = Future.successful(true)

  override def findById(nino: Nino, taxYear: TaxYear, id: SourceId): Future[Option[T]] = Future.successful(Some(example(id)))

  override def delete(nino: Nino, taxYear: TaxYear, id: SourceId) = Future.successful(true)

  override def delete(nino: Nino, taxYear: TaxYear) = Future.successful(true)

  override def list(nino: Nino, taxYear: TaxYear): Future[Seq[T]] = {
    Future.successful{
      Seq(
        example(generateId),
        example(generateId),
        example(generateId),
        example(generateId),
        example(generateId)
      )
    }
  }

}
