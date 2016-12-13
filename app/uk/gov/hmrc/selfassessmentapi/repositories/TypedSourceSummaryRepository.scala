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

import play.api.libs.json.Format
import reactivemongo.bson.{BSONDocument, BSONString}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.AtomicUpdate
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{SourceMetadata, Summary}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


trait TypedSourceSummaryRepository[A <: SourceMetadata, ID <: Any] extends TypedSourceRepository[A, ID] with AtomicUpdate[A] {

  implicit val domainFormatImplicit: Format[A]

  def createSummary(nino: Nino, taxYear: TaxYear, sourceId: SourceId, summary: Summary): Future[Option[SummaryId]] = {
    val modifiers = BSONDocument(Seq(
      modifierStatementLastModified,
      "$push" -> BSONDocument(summary.arrayName -> summary.toBsonDocument)
    ))

    for {
      result <- atomicUpdate(
        BSONDocument("nino" -> BSONString(nino.toString), "taxYear" -> BSONString(taxYear.toString), "sourceId" -> BSONString(sourceId)),
        modifiers
      )
    } yield result.map(x => summary.summaryId)
  }

  def updateSummary(nino: Nino, taxYear: TaxYear, sourceId: SourceId, summary: Summary, exists: A => Boolean): Future[Boolean] = {
    lazy val modifiers = BSONDocument(Seq(
      modifierStatementLastModified,
      "$set" -> BSONDocument(summary.arrayName+".$" -> summary.toBsonDocument)
    ))

    findMongoObjectById(nino, taxYear, sourceId).flatMap { mongoObjectOption =>
      mongoObjectOption.map { y =>
        if (exists(y))
          atomicUpdate(
            BSONDocument("nino" -> BSONString(nino.toString), "taxYear" -> BSONString(taxYear.toString), "sourceId" -> BSONString(sourceId), s"${summary.arrayName}.summaryId" -> BSONString(summary.summaryId)),
            modifiers
          ).map(_.isDefined)
        else
          Future.successful(false)
      } getOrElse Future.successful(false)
    }
  }


  def deleteSummary(nino: Nino, taxYear: TaxYear, sourceId: SourceId, id: SummaryId, arrayName: String, exists: A => Boolean): Future[Boolean] = {
    lazy val modifiers = BSONDocument(Seq(
      modifierStatementLastModified,
      "$pull" -> BSONDocument(arrayName -> BSONDocument("summaryId" -> id))
    ))

    findMongoObjectById(nino, taxYear, sourceId).flatMap { mongoObjectOption =>
      mongoObjectOption.map { y =>
        if(exists(y))
          atomicUpdate(
            BSONDocument("nino" -> BSONString(nino.toString), "taxYear" -> BSONString(taxYear.toString), "sourceId" -> BSONString(sourceId)),
            modifiers
          ).map(_.isDefined)
        else
          Future.successful(false)
      } getOrElse Future.successful(false)
    }
  }


  def listSummaries[U](nino: Nino, taxYear: TaxYear, sourceId: SourceId, finder: A => Seq[U]): Future[Option[Seq[U]]] =
    for(option <- findMongoObjectById(nino, taxYear, sourceId)) yield option.map(finder)

  def findSummaryById[U](nino: Nino, taxYear: TaxYear, sourceId: SourceId, finder: A => Option[U]): Future[Option[U]] =
    for(option <- findMongoObjectById(nino, taxYear, sourceId)) yield option.flatMap(finder)

}
