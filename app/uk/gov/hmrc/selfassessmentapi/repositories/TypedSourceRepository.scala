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

import org.joda.time.{DateTime, DateTimeZone}
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONObjectID}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.Repository
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.SourceMetadata

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


trait TypedSourceRepository[A <: SourceMetadata, ID <: Any] extends Repository[A, ID] {

  protected def modifierStatementLastModified: (String, BSONDocument) =
    "$set" -> BSONDocument("lastModifiedDateTime" -> BSONDateTime(DateTime.now(DateTimeZone.UTC).getMillis))

  def isInsertion(suppliedId: BSONObjectID, returned: A): Boolean =
    suppliedId.equals(BSONObjectID(returned.sourceId))

  def findMongoObjectById(saUtr: SaUtr, taxYear: TaxYear, id: SourceId): Future[Option[A]] = {
    find("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear, "sourceId" -> id).map(_.headOption)
  }

  def delete(saUtr: SaUtr, taxYear: TaxYear, id: SourceId): Future[Boolean] = {
    for (option <- remove("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear, "sourceId" -> id)) yield option.n == 1
  }

  def delete(saUtr: SaUtr, taxYear: TaxYear): Future[Boolean] = {
    for (option <- remove("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear)) yield option.n > 0
  }
}
